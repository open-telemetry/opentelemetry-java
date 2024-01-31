/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.concurrent.ExecutorService;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/** A builder for {@link PrometheusHttpServer}. */
public final class PrometheusHttpServerBuilder {

  static final int DEFAULT_PORT = 9464;
  private static final String DEFAULT_HOST = "0.0.0.0";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private PrometheusRegistry prometheusRegistry = new PrometheusRegistry();
  private boolean otelScopeEnabled = true;
  private boolean addResourceAttributesAsLabels = false;
  private Predicate<String> allowedResourceAttributesFilter = attributeKey -> true;
  @Nullable private ExecutorService executor;

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
  public PrometheusHttpServerBuilder setPrometheusRegistry(PrometheusRegistry prometheusRegistry) {
    requireNonNull(prometheusRegistry, "prometheusRegistry");
    this.prometheusRegistry = prometheusRegistry;
    return this;
  }

  /** Set if the {@code otel_scope_*} attributes are generated. Default is {@code true}. */
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
    this.addResourceAttributesAsLabels = true;
    return this;
  }

  /**
   * Returns a new {@link PrometheusHttpServer} with the configuration of this builder which can be
   * registered with a {@link io.opentelemetry.sdk.metrics.SdkMeterProvider}.
   */
  public PrometheusHttpServer build() {
    return new PrometheusHttpServer(
        host,
        port,
        executor,
        prometheusRegistry,
        otelScopeEnabled,
        addResourceAttributesAsLabels,
        allowedResourceAttributesFilter);
  }

  PrometheusHttpServerBuilder() {}
}
