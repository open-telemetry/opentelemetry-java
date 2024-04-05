/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.common.export.MemoryMode;
import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** A builder for {@link PrometheusHttpServer}. */
public final class PrometheusHttpServerBuilder {

  static final int DEFAULT_PORT = 9464;
  private static final String DEFAULT_HOST = "0.0.0.0";
  private static final MemoryMode DEFAULT_MEMORY_MODE = MemoryMode.IMMUTABLE_DATA;

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private PrometheusRegistry prometheusRegistry = new PrometheusRegistry();
  private boolean otelScopeEnabled = true;
  @Nullable private Predicate<String> allowedResourceAttributesFilter;
  @Nullable private ExecutorService executor;
  private MemoryMode memoryMode = DEFAULT_MEMORY_MODE;

  PrometheusHttpServerBuilder() {}

  PrometheusHttpServerBuilder(PrometheusHttpServerBuilder builder) {
    this.host = builder.host;
    this.port = builder.port;
    this.prometheusRegistry = builder.prometheusRegistry;
    this.otelScopeEnabled = builder.otelScopeEnabled;
    this.allowedResourceAttributesFilter = builder.allowedResourceAttributesFilter;
    this.executor = builder.executor;
  }

  /** Sets the host to bind to. If unset, defaults to {@value #DEFAULT_HOST}. */
  public PrometheusHttpServerBuilder setHost(String host) {
    requireNonNull(host, "host");
    checkArgument(!host.isEmpty(), "host must not be empty");
    this.host = host;
    return this;
  }

  /** Sets the port to bind to. If unset, defaults to {@value #DEFAULT_PORT}. */
  public PrometheusHttpServerBuilder setPort(int port) {
    checkArgument(port >= 0, "port must be positive");
    this.port = port;
    return this;
  }

  /** Sets the {@link ExecutorService} to be used for {@link PrometheusHttpServer}. */
  public PrometheusHttpServerBuilder setExecutor(ExecutorService executor) {
    requireNonNull(executor, "executor");
    this.executor = executor;
    return this;
  }

  /** Sets the {@link PrometheusRegistry} to be used for {@link PrometheusHttpServer}. */
  @SuppressWarnings("UnusedReturnValue")
  public PrometheusHttpServerBuilder setPrometheusRegistry(PrometheusRegistry prometheusRegistry) {
    requireNonNull(prometheusRegistry, "prometheusRegistry");
    this.prometheusRegistry = prometheusRegistry;
    return this;
  }

  /** Set if the {@code otel_scope_*} attributes are generated. Default is {@code true}. */
  @SuppressWarnings("UnusedReturnValue")
  public PrometheusHttpServerBuilder setOtelScopeEnabled(boolean otelScopeEnabled) {
    this.otelScopeEnabled = otelScopeEnabled;
    return this;
  }

  /**
   * Set if the resource attributes should be added as labels on each exported metric.
   *
   * <p>If set, resource attributes will be added as labels on each exported metric if their key
   * tests positive (true) when passed through {@code resourceAttributesFilter}.
   *
   * @param resourceAttributesFilter a predicate that returns true if the resource attribute should
   *     be added as a label on each exported metric. The predicates input is the resource attribute
   *     key.
   */
  public PrometheusHttpServerBuilder setAllowedResourceAttributesFilter(
      Predicate<String> resourceAttributesFilter) {
    this.allowedResourceAttributesFilter = requireNonNull(resourceAttributesFilter);
    return this;
  }

  /** Set the {@link MemoryMode}. */
  public PrometheusHttpServerBuilder setMemoryMode(MemoryMode memoryMode) {
    requireNonNull(memoryMode, "memoryMode");
    this.memoryMode = memoryMode;
    return this;
  }

  /**
   * Returns a new {@link PrometheusHttpServer} with the configuration of this builder which can be
   * registered with a {@link io.opentelemetry.sdk.metrics.SdkMeterProvider}.
   */
  public PrometheusHttpServer build() {
    return new PrometheusHttpServer(
        new PrometheusHttpServerBuilder(this), // copy to prevent modification
        host,
        port,
        executor,
        prometheusRegistry,
        otelScopeEnabled,
        allowedResourceAttributesFilter,
        memoryMode);
  }
}
