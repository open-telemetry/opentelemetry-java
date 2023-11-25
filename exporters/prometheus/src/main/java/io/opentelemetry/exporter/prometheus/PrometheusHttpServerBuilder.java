/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.prometheus.metrics.model.registry.PrometheusRegistry;
import java.util.concurrent.ExecutorService;
import javax.annotation.Nullable;

/** A builder for {@link PrometheusHttpServer}. */
public final class PrometheusHttpServerBuilder {

  static final int DEFAULT_PORT = 9464;
  private static final String DEFAULT_HOST = "0.0.0.0";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;
  private PrometheusRegistry prometheusRegistry = PrometheusRegistry.defaultRegistry;

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

  /**
   * Returns a new {@link PrometheusHttpServer} with the configuration of this builder which can be
   * registered with a {@link io.opentelemetry.sdk.metrics.SdkMeterProvider}.
   */
  public PrometheusHttpServer build() {
    return new PrometheusHttpServer(host, port, executor, prometheusRegistry);
  }

  PrometheusHttpServerBuilder() {}
}
