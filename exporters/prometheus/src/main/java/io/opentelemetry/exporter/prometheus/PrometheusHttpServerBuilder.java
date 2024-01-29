/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
  private Pattern allowedResourceAttributesRegexp = Pattern.compile(".*");
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
   * If set to true, resource attributes will be added as labels on each exported metric. Default is
   * {@code false}.
   *
   * <p>You can limit the attributes that are added as labels by setting a regular expression in
   * {@link #setAllowedResourceAttributesRegexp(String)}.
   */
  public PrometheusHttpServerBuilder setAddResourceAttributesAsLabels(
      boolean addResourceAttributesAsLabels) {
    this.addResourceAttributesAsLabels = addResourceAttributesAsLabels;
    return this;
  }

  /**
   * Sets a regular expression to limit the resource attributes that are added as labels.
   *
   * <p>If not set, all resource attributes will be added as labels on each exported metric, unless
   * {@link #setAddResourceAttributesAsLabels(boolean)} is set to false. If set, only resource
   * attributes that match the regular expression will be added as labels.
   *
   * @param resourceAttributesRegexp a regular expression matching {@link java.util.regex.Pattern}
   *     rules
   * @throws PatternSyntaxException if the {@code resourceAttributesRegexp}'s syntax is not valid
   */
  public PrometheusHttpServerBuilder setAllowedResourceAttributesRegexp(
      String resourceAttributesRegexp) {
    this.allowedResourceAttributesRegexp = Pattern.compile(resourceAttributesRegexp);
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
        allowedResourceAttributesRegexp);
  }

  PrometheusHttpServerBuilder() {}
}
