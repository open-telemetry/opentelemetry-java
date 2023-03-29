/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.internal.DaemonThreadFactory;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.annotation.Nullable;

/** A builder for {@link PrometheusHttpServer}. */
public final class PrometheusHttpServerBuilder {

  static final int DEFAULT_PORT = 9464;
  private static final String DEFAULT_HOST = "0.0.0.0";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;

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

  /**
   * Returns a new {@link PrometheusHttpServer} with the configuration of this builder which can be
   * registered with a {@link io.opentelemetry.sdk.metrics.SdkMeterProvider}.
   */
  public PrometheusHttpServer build() {
    ExecutorService executorService = this.executor;
    if (executorService == null) {
      executorService = getDefaultExecutor();
    }
    return new PrometheusHttpServer(host, port, executorService);
  }

  PrometheusHttpServerBuilder() {}

  private static ExecutorService getDefaultExecutor() {
    return Executors.newFixedThreadPool(5, new DaemonThreadFactory("prometheus-http"));
  }
}
