/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.trace.zpages;

import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.SdkTracerProviderBuilder;
import io.opentelemetry.sdk.trace.SpanLimits;
import io.opentelemetry.sdk.trace.SpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A collection of HTML pages to display stats and trace data and allow library configuration
 * control. To use, add {@linkplain ZPageServer#getSpanProcessor() the z-pages span processor},
 * {@linkplain ZPageServer#getTracezTraceConfigSupplier() the z-pages dynamic trace config} and
 * {@linkplain ZPageServer#getTracezSampler() the z-pages dynamic sampler} to a {@link
 * io.opentelemetry.sdk.trace.SdkTracerProviderBuilder}. Currently all tracers can only be made
 * visible to a singleton {@link ZPageServer}.
 *
 * <p>Example usage with private {@link HttpServer}
 *
 * <pre>{@code
 * public class Main {
 *   public static void main(String[] args) throws Exception {
 *       OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
 *         .setTracerProvider(
 *             SdkTracerProvider.builder()
 *                 .addSpanProcessor(ZPageServer.getSpanProcessor())
 *                 .setSpanLimits(ZPageServer.getTracezTraceConfigSupplier())
 *                 .setSampler(ZPageServer.getTracezSampler())
 *                 .build())
 *         .build();
 *
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
 *       OpenTelemetry openTelemetry = OpenTelemetrySdk.builder()
 *         .setTracerProvider(
 *             SdkTracerProvider.builder()
 *                 .addSpanProcessor(ZPageServer.getSpanProcessor())
 *                 .setSpanLimits(ZPageServer.getTracezTraceConfigSupplier())
 *                 .setSampler(ZPageServer.getTracezSampler())
 *                 .build())
 *         .build();
 *
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
  private final TracezSpanProcessor tracezSpanProcessor = TracezSpanProcessor.builder().build();
  private final TracezTraceConfigSupplier tracezTraceConfigSupplier =
      new TracezTraceConfigSupplier();
  private final TracezDataAggregator tracezDataAggregator =
      new TracezDataAggregator(tracezSpanProcessor);
  // Handler for /tracez page
  private final ZPageHandler tracezZPageHandler = new TracezZPageHandler(tracezDataAggregator);
  // Handler for /traceconfigz page
  private final ZPageHandler traceConfigzZPageHandler =
      new TraceConfigzZPageHandler(tracezTraceConfigSupplier);
  // Handler for index page, **please include all available ZPageHandlers in the constructor**
  private final ZPageHandler indexZPageHandler =
      new IndexZPageHandler(Arrays.asList(tracezZPageHandler, traceConfigzZPageHandler));

  private final Object mutex = new Object();

  @GuardedBy("mutex")
  @Nullable
  private HttpServer server;

  private ZPageServer() {}

  public static ZPageServer create() {
    return new ZPageServer();
  }

  /** Returns a supplier of {@link SpanLimits} which can be reconfigured using zpages. */
  public Supplier<SpanLimits> getTracezTraceConfigSupplier() {
    return tracezTraceConfigSupplier;
  }

  /** Returns a {@link Sampler} which can be reconfigured using zpages. */
  public Sampler getTracezSampler() {
    return tracezTraceConfigSupplier;
  }

  /**
   * Returns a {@link SpanProcessor} which will allow processing of spans by {@link ZPageServer}.
   */
  public SpanProcessor getSpanProcessor() {
    return tracezSpanProcessor;
  }

  /**
   * Registers a {@link ZPageHandler} for the index page of zPages. The page displays information
   * about all available zPages with links to those zPages.
   *
   * @param server the {@link HttpServer} for the page to register to.
   */
  private void registerIndexZPageHandler(HttpServer server) {
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
  private void registerTracezZPageHandler(HttpServer server) {
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
  private void registerTraceConfigzZPageHandler(HttpServer server) {
    server.createContext(
        traceConfigzZPageHandler.getUrlPath(), new ZPageHttpHandler(traceConfigzZPageHandler));
  }

  /**
   * Registers all zPages to the given {@link HttpServer} {@code server}.
   *
   * @param server the {@link HttpServer} for the page to register to.
   */
  public void registerAllPagesToHttpServer(HttpServer server) {
    // For future zPages, register them to the server in here
    registerIndexZPageHandler(server);
    registerTracezZPageHandler(server);
    registerTraceConfigzZPageHandler(server);
    ZPageLogo.registerStaticResources(server);
  }

  /** Method for stopping the {@link HttpServer} {@code server}. */
  private void stop() {
    synchronized (mutex) {
      if (server == null) {
        return;
      }
      server.stop(HTTPSERVER_STOP_DELAY);
      server = null;
    }
  }

  /**
   * Convenience method to return a new SdkTracerProvider that has been configured with our ZPage
   * specific span processor, sampler, and limits.
   *
   * @return new SdkTracerProvider
   */
  public SdkTracerProvider buildSdkTracerProvider() {
    return buildSdkTracerProvider(SdkTracerProvider.builder());
  }

  /**
   * Convenience method to return a new SdkTracerProvider that has been configured with our ZPage
   * specific span processor, sampler, and limits.
   *
   * @return new SdkTracerProvider
   */
  public SdkTracerProvider buildSdkTracerProvider(SdkTracerProviderBuilder builder) {
    return builder
        .addSpanProcessor(getSpanProcessor())
        .setSpanLimits(getTracezTraceConfigSupplier())
        .setSampler(getTracezSampler())
        .build();
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
  public void startHttpServerAndRegisterAllPages(int port) throws IOException {
    synchronized (mutex) {
      if (server != null) {
        throw new IllegalStateException("The HttpServer is already started.");
      }
      server = HttpServer.create(new InetSocketAddress(port), HTTPSERVER_BACKLOG);
      registerAllPagesToHttpServer(server);
      server.start();
    }

    Runtime.getRuntime()
        .addShutdownHook(
            new Thread() {
              @Override
              public void run() {
                ZPageServer.this.stop();
              }
            });
  }
}
