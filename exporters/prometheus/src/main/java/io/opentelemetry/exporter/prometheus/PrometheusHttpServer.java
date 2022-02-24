/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

// Prometheus instrumentation library for JVM applications
// Copyright 2012-2015 The Prometheus Authors

package io.opentelemetry.exporter.prometheus;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import java.io.Closeable;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.zip.GZIPOutputStream;
import javax.annotation.Nullable;

/**
 * A {@link MetricExporter} that starts an HTTP server that will collect metrics and serialize to
 * Prometheus text format on request.
 */
// Very similar to
// https://github.com/prometheus/client_java/blob/master/simpleclient_httpserver/src/main/java/io/prometheus/client/exporter/HTTPServer.java
public final class PrometheusHttpServer implements Closeable, MetricReader {

  private static final DaemonThreadFactory THREAD_FACTORY =
      new DaemonThreadFactory("prometheus-http");

  private final HttpServer server;
  private final ExecutorService executor;

  /**
   * Returns a new {@link MetricReaderFactory} which can be registered to an {@link
   * io.opentelemetry.sdk.metrics.SdkMeterProvider} to expose Prometheus metrics on port {@value
   * PrometheusHttpServerBuilder#DEFAULT_PORT}.
   */
  public static MetricReaderFactory newMetricReaderFactory() {
    return builder().newMetricReaderFactory();
  }

  /** Returns a new {@link PrometheusHttpServerBuilder}. */
  public static PrometheusHttpServerBuilder builder() {
    return new PrometheusHttpServerBuilder();
  }

  PrometheusHttpServer(String host, int port, MetricProducer producer) {
    try {
      server = HttpServer.create(new InetSocketAddress(host, port), 3);
    } catch (IOException e) {
      throw new UncheckedIOException("Could not create Prometheus HTTP server", e);
    }
    server.createContext("/", new MetricsHandler(producer));
    server.createContext("/metrics", new MetricsHandler(producer));
    server.createContext("/-/healthy", HealthHandler.INSTANCE);

    executor = Executors.newFixedThreadPool(5, THREAD_FACTORY);
    server.setExecutor(executor);

    start();
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
  public AggregationTemporality getPreferredTemporality() {
    return AggregationTemporality.CUMULATIVE;
  }

  @Override
  public CompletableResultCode flush() {
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

  // Visible for testing.
  InetSocketAddress getAddress() {
    return server.getAddress();
  }

  private static class MetricsHandler implements HttpHandler {

    private final MetricProducer producer;

    private MetricsHandler(MetricProducer producer) {
      this.producer = producer;
    }

    @Override
    public void handle(HttpExchange exchange) throws IOException {
      Collection<MetricData> metrics = producer.collectAllMetrics();
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
        serializer.write(metrics, out);
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
