/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import io.opentelemetry.sdk.metrics.export.MetricProducer;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.export.MetricReaderFactory;
import java.net.InetSocketAddress;
import javax.annotation.Nullable;

/**
 * Package private {@link MetricReaderFactory} to allow tests to be able to read the address of a
 * started {@link PrometheusHttpServer}.
 */
class PrometheusHttpServerFactory implements MetricReaderFactory {

  private final String host;
  private final int port;
  @Nullable private PrometheusHttpServer server;

  PrometheusHttpServerFactory(String host, int port) {
    this.host = host;
    this.port = port;
  }

  @Override
  public MetricReader apply(MetricProducer producer) {
    return server = new PrometheusHttpServer(host, port, producer);
  }

  // Visible for testing
  InetSocketAddress getAddress() {
    PrometheusHttpServer server = this.server;
    if (server != null) {
      return server.getAddress();
    }
    throw new IllegalStateException(
        "Server not started. Call after SdkMeterProviderBuilder.build()");
  }
}
