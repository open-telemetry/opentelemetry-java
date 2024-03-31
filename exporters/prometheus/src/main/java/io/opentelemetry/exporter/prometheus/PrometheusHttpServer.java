/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:

// Prometheus instrumentation library for JVM applications
// Copyright 2012-2015 The Prometheus Authors

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.exporter.httpserver.MetricsHandler;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * A {@link MetricReader} that starts an HTTP server that will collect metrics and serialize to
 * Prometheus text format on request.
 */
public final class PrometheusHttpServer implements MetricReader {

  private final HTTPServer httpServer;
  private final PrometheusMetricReader prometheusMetricReader;
  private final PrometheusRegistry prometheusRegistry;
  private final String host;
  private final PrometheusHttpServerBuilder builder;

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

  PrometheusHttpServer(
      PrometheusHttpServerBuilder builder,
      String host,
      int port,
      @Nullable ExecutorService executor,
      PrometheusRegistry prometheusRegistry,
      boolean otelScopeEnabled,
      @Nullable Predicate<String> allowedResourceAttributesFilter) {
    this.builder = builder;
    this.prometheusMetricReader =
        new PrometheusMetricReader(otelScopeEnabled, allowedResourceAttributesFilter);
    this.host = host;
    this.prometheusRegistry = prometheusRegistry;
    prometheusRegistry.register(prometheusMetricReader);
    try {
      this.httpServer =
          HTTPServer.builder()
              .hostname(host)
              .port(port)
              .executorService(executor)
              .registry(prometheusRegistry)
              .defaultHandler(new MetricsHandler(prometheusRegistry))
              .buildAndStart();
    } catch (IOException e) {
      throw new UncheckedIOException("Could not create Prometheus HTTP server", e);
    }
  }

  @Override
  public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
    return prometheusMetricReader.getAggregationTemporality(instrumentType);
  }

  @Override
  public void register(CollectionRegistration registration) {
    prometheusMetricReader.register(registration);
  }

  @Override
  public CompletableResultCode forceFlush() {
    return prometheusMetricReader.forceFlush();
  }

  @Override
  public CompletableResultCode shutdown() {
    CompletableResultCode result = new CompletableResultCode();
    Runnable shutdownFunction =
        () -> {
          try {
            prometheusRegistry.unregister(prometheusMetricReader);
            httpServer.stop();
            prometheusMetricReader.shutdown().whenComplete(result::succeed);
          } catch (Throwable t) {
            result.fail();
          }
        };
    Thread shutdownThread = new Thread(shutdownFunction, "prometheus-httpserver-shutdown");
    shutdownThread.setDaemon(true);
    shutdownThread.start();
    return result;
  }

  @Override
  public void close() {
    shutdown().join(10, TimeUnit.SECONDS);
  }

  @Override
  public String toString() {
    return "PrometheusHttpServer{address=" + getAddress() + "}";
  }

  /**
   * Returns a new {@link PrometheusHttpServerBuilder} with the same configuration as this instance.
   */
  public PrometheusHttpServerBuilder toBuilder() {
    return new PrometheusHttpServerBuilder(builder);
  }

  // Visible for testing.
  InetSocketAddress getAddress() {
    return new InetSocketAddress(host, httpServer.getPort());
  }
}
