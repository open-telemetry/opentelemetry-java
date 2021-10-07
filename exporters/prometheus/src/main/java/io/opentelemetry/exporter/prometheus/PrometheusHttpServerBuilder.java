/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;

/** A builder for {@link PrometheusHttpServer}. */
public final class PrometheusHttpServerBuilder {

  private static final int DEFAULT_PORT = 9464;
  private static final String DEFAULT_HOST = "0.0.0.0";

  private String host = DEFAULT_HOST;
  private int port = DEFAULT_PORT;

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

  /** Returns a new {@link PrometheusHttpServer} with the configuration of this builder. */
  public MetricReaderFactory build() {
    return producer -> new PrometheusHttpServer(host, port, producer);
  }

  PrometheusHttpServerBuilder() {}
}
