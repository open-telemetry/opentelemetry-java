/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.sdk.SdkOpenTelemetry;
import io.opentelemetry.sdk.trace.SdkTracerManagement;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A collection of HTML pages to display stats and trace data and allow library configuration
 * control.
 *
 * <p>Example usage with private {@link HttpServer}
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *     ZPageServer.startHttpServerAndRegisterAllPages(8000);
 *     ... // do work
 *   }
 * }
 * }</pre>
 *
 * <p>Example usage with shared {@link HttpServer}
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *     HttpServer server = HttpServer.create(new InetSocketAddress(8000), 10);
 *     ZPageServer.registerAllPagesToHttpServer(server);
 *     server.start();
 *     ... // do work
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public final class ZPageServer {
  // The maximum number of queued incoming connections allowed on the HttpServer listening socket.
  private static final int HTTPSERVER_BACKLOG = 5;
  // Length of time to wait for the HttpServer to stop
  private static final int HTTPSERVER_STOP_DELAY = 1;
  // Tracez SpanProcessor and DataAggregator for constructing TracezZPageHandler
  private static final TracezSpanProcessor tracezSpanProcessor =
      TracezSpanProcessor.builder().build();
  private static final TracezDataAggregator tracezDataAggregator =
      new TracezDataAggregator(tracezSpanProcessor);
  private static final SdkTracerManagement TRACER_SDK_MANAGEMENT =
      SdkOpenTelemetry.getGlobalTracerManagement();
  // Handler for /tracez page
  private static final ZPageHandler tracezZPageHandler =
      new TracezZPageHandler(tracezDataAggregator);
  // Handler for /traceconfigz page
  private static final ZPageHandler traceConfigzZPageHandler =
      new TraceConfigzZPageHandler(TRACER_SDK_MANAGEMENT);
  // Handler for index page, **please include all available ZPageHandlers in the constructor**
  private static final ZPageHandler indexZPageHandler =
      new IndexZPageHandler(Arrays.asList(tracezZPageHandler, traceConfigzZPageHandler));

  private static final Object mutex = new Object();
  private static final AtomicBoolean isTracezSpanProcesserAdded = new AtomicBoolean(false);

  @GuardedBy("mutex")
  @Nullable
  private static HttpServer server;

  /** Function that adds the {@link TracezSpanProcessor} to the {@link SdkTracerManagement}. */
  private static void addTracezSpanProcessor() {
    if (isTracezSpanProcesserAdded.compareAndSet(/* expectedValue=*/ false, /* newValue=*/ true)) {
      TRACER_SDK_MANAGEMENT.addSpanProcessor(tracezSpanProcessor);
    }
  }

  /**
   * Registers a {@link ZPageHandler} for the index page of zPages. The page displays information
   * about all available zPages with links to those zPages.
   *
   * @param server the {@link HttpServer} for the page to register to.
   */
  static void registerIndexZPageHandler(HttpServer server) {
    server.createContext(indexZPageHandler.getUrlPath(), new ZPageHttpHandler(indexZPageHandler));
  }

  /**
   * Registers a {@link ZPageHandler} for tracing debug to the server. The page displays information
   * about all running spans and all sampled spans based on latency and error.
   *
   * <p>It displays a summary table which contains one row for each span name and data about number
   * of running and sampled spans.
   *
   * <p>Clicking on a cell in the table with a number that is greater than zero will display
   * detailed information about that span.
   *
   * <p>This method will add the TracezSpanProcessor to the tracerProvider, it should only be called
   * once.
   *
   * @param server the {@link HttpServer} for the page to register to.
   */
  static void registerTracezZPageHandler(HttpServer server) {
    addTracezSpanProcessor();
    server.createContext(tracezZPageHandler.getUrlPath(), new ZPageHttpHandler(tracezZPageHandler));
  }

  /**
   * Registers a {@code ZPageHandler} for tracing config. The page displays information about all
   * active configuration and allow changing the active configuration.
   *
   * <p>It displays a change table which allows users to change active configuration.
   *
   * <p>It displays an active value table which displays current active configuration.
   *
   * <p>Refreshing the page will show the updated active configuration.
   *
   * @param server the {@link HttpServer} for the page to register to.
   */
  static void registerTraceConfigzZPageHandler(HttpServer server) {
    server.createContext(
        traceConfigzZPageHandler.getUrlPath(), new ZPageHttpHandler(traceConfigzZPageHandler));
  }

  /**
   * Registers all zPages to the given {@link HttpServer} {@code server}.
   *
   * @param server the {@link HttpServer} for the page to register to.
   */
  public static void registerAllPagesToHttpServer(HttpServer server) {
    // For future zPages, register them to the server in here
    registerIndexZPageHandler(server);
    registerTracezZPageHandler(server);
    registerTraceConfigzZPageHandler(server);
  }

  /** Method for stopping the {@link HttpServer} {@code server}. */
  private static void stop() {
    synchronized (mutex) {
      if (server == null) {
        return;
      }
      server.stop(HTTPSERVER_STOP_DELAY);
      server = null;
    }
  }

  /**
   * Starts a private {@link HttpServer} and registers all zPages to it. When the JVM shuts down the
   * server is stopped.
   *
   * <p>Users can only call this function once per process.
   *
   * @param port the port used to bind the {@link HttpServer} {@code server}
   * @throws IllegalStateException if the server is already started.
   * @throws IOException if the server cannot bind to the specified port.
   */
  public static void startHttpServerAndRegisterAllPages(int port) throws IOException {
    synchronized (mutex) {
      if (server != null) {
        throw new IllegalStateException("The HttpServer is already started.");
      }
      server = HttpServer.create(new InetSocketAddress(port), HTTPSERVER_BACKLOG);
      ZPageServer.registerAllPagesToHttpServer(server);
      server.start();
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                ZPageServer.stop();
              }
            });
  }

  /**
   * Returns the boolean indicating if TracezSpanProcessor is added. For testing purpose only.
   *
   * @return the boolean indicating if TracezSpanProcessor is added.
   */
  // Visible for testing
  static boolean getIsTracezSpanProcesserAdded() {
    return isTracezSpanProcesserAdded.get();
  }

  private ZPageServer() {}
}
