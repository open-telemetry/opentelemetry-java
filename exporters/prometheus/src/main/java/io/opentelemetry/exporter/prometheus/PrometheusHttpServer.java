/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

// Prometheus instrumentation library for JVM applications
// Copyright 2012-2015 The Prometheus Authors

package io.opentelemetry.exporter.prometheus;

import static java.util.stream.Collectors.joining;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.export.MetricProducer;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;

/**
 * A {@link MetricReader} that starts an HTTP server that will collect metrics and serialize to
 * Prometheus text format on request.
 */
// Very similar to
// https://github.com/prometheus/client_java/blob/master/simpleclient_httpserver/src/main/java/io/prometheus/client/exporter/HTTPServer.java
public final class PrometheusHttpServer implements MetricReader {

  private static final DaemonThreadFactory THREAD_FACTORY =
      new DaemonThreadFactory("prometheus-http");
  private static final Logger LOGGER = Logger.getLogger(PrometheusHttpServer.class.getName());

  private final HttpServer server;
  private final ExecutorService executor;
  private volatile MetricProducer metricProducer = MetricProducer.noop();

  /**
   * Returns a new {@link PrometheusHttpServer} which can be registered to an {@link
   * io.opentelemetry.sdk.metrics.SdkMeterProvider} to expose Prometheus metrics on port {@value
   * PrometheusHttpServerBuilder#DEFAULT_PORT}.
   */
  public static PrometheusHttpServer create() {
    return builder().build();
  }

  /** Returns a new {@link PrometheusHttpServerBuilder}. */
  public static PrometheusHttpServerBuilder builder() {
    return new PrometheusHttpServerBuilder();
  }

  PrometheusHttpServer(String host, int port, ExecutorService executor) {
    try {
      server = HttpServer.create(new InetSocketAddress(host, port), 3);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not create Prometheus HTTP server", e);
    }
    MetricsHandler metricsHandler =
        new MetricsHandler(() -> getMetricProducer().collectAllMetrics());
    server.createContext("/", metricsHandler);
    server.createContext("/metrics", metricsHandler);
    server.createContext("/-/healthy", HealthHandler.INSTANCE);
    this.executor = executor;
    server.setExecutor(executor);

    start();
  }

  private MetricProducer getMetricProducer() {
    return metricProducer;
  }

  private void start() {
    // server.start must be called from a daemon thread for it to be a daemon.
    if (Thread.currentThread().isDaemon()) {
      server.start();
      return;
    }

    Thread thread = THREAD_FACTORY.newThread(server::start);
    thread.start();
    try {
      thread.join();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
    }
  }

  @Override
  public void register(CollectionRegistration registration) {
    this.metricProducer = MetricProducer.asMetricProducer(registration);
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return AggregationTemporality.CUMULATIVE;
  }

  @Override
  public CompletableResultCode forceFlush() {
    return CompletableResultCode.ofSuccess();
  }

  @Override
  public CompletableResultCode shutdown() {
    CompletableResultCode result = new CompletableResultCode();
    Thread thread =
        THREAD_FACTORY.newThread(
            () -> {
              try {
                server.stop(10);
                executor.shutdownNow();
              } catch (Throwable t) {
                result.fail();
                return;
              }
              result.succeed();
            });
    thread.start();
    return result;
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "PrometheusHttpServer{address=" + server.getAddress() + "}";
  }

  // Visible for testing.
  InetSocketAddress getAddress() {
    return server.getAddress();
  }

  private static class MetricsHandler implements HttpHandler {

    private final Set<String> allConflictHeaderNames =
        Collections.newSetFromMap(new ConcurrentHashMap<>());

    private final Supplier<Collection<MetricData>> metricsSupplier;

    private MetricsHandler(Supplier<Collection<MetricData>> metricsSupplier) {
      this.metricsSupplier = metricsSupplier;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      Collection<MetricData> metrics = metricsSupplier.get();
      Set<String> requestedNames = parseQuery(exchange.getRequestURI().getRawQuery());
      Predicate<String> filter =
          requestedNames.isEmpty() ? unused -> true : requestedNames::contains;
      Serializer serializer =
          Serializer.create(exchange.getRequestHeaders().getFirst("Accept"), filter);
      exchange.getResponseHeaders().set("Content-Type", serializer.contentType());

      boolean compress = shouldUseCompression(exchange);
      if (compress) {
        exchange.getResponseHeaders().set("Content-Encoding", "gzip");
      }

      if (exchange.getRequestMethod().equals("HEAD")) {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
      } else {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, 0);
        OutputStream out;
        if (compress) {
          out = new GZIPOutputStream(exchange.getResponseBody());
        } else {
          out = exchange.getResponseBody();
        }
        Set<String> conflictHeaderNames = serializer.write(metrics, out);
        conflictHeaderNames.removeAll(allConflictHeaderNames);
        if (conflictHeaderNames.size() > 0 && LOGGER.isLoggable(Level.WARNING)) {
          LOGGER.log(
              Level.WARNING,
              "Metric conflict(s) detected. Multiple metrics with same name but different type: "
                  + conflictHeaderNames.stream().collect(joining(",", "[", "]")));
          allConflictHeaderNames.addAll(conflictHeaderNames);
        }
      }
      exchange.close();
    }
  }

  private static boolean shouldUseCompression(HttpExchange exchange) {
    List<String> encodingHeaders = exchange.getRequestHeaders().get("Accept-Encoding");
    if (encodingHeaders == null) {
      return false;
    }

    for (String encodingHeader : encodingHeaders) {
      String[] encodings = encodingHeader.split(",");
      for (String encoding : encodings) {
        if (encoding.trim().equalsIgnoreCase("gzip")) {
          return true;
        }
      }
    }
    return false;
  }

  private static Set<String> parseQuery(@Nullable String query) throws IOException {
    if (query == null) {
      return Collections.emptySet();
    }
    Set<String> names = new HashSet<>();
    String[] pairs = query.split("&");
    for (String pair : pairs) {
      int idx = pair.indexOf("=");
      if (idx != -1 && URLDecoder.decode(pair.substring(0, idx), "UTF-8").equals("name[]")) {
        names.add(URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
      }
    }
    return names;
  }

  private enum HealthHandler implements HttpHandler {
    INSTANCE;

    private static final byte[] RESPONSE = "Exporter is Healthy.".getBytes(StandardCharsets.UTF_8);
    private static final String CONTENT_LENGTH_VALUE = String.valueOf(RESPONSE.length);

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      exchange.getResponseHeaders().set("Content-Length", CONTENT_LENGTH_VALUE);
      if (exchange.getRequestMethod().equals("HEAD")) {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, -1);
      } else {
        exchange.sendResponseHeaders(HttpURLConnection.HTTP_OK, RESPONSE.length);
        exchange.getResponseBody().write(RESPONSE);
      }
      exchange.close();
    }
  }
}
