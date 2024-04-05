/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConfigurableMetricReaderProvider;
import io.opentelemetry.sdk.metrics.export.MetricReader;

/**
 * SPI implementation for {@link PrometheusHttpServer}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class PrometheusMetricReaderProvider implements ConfigurableMetricReaderProvider {

  @Override
  public MetricReader createMetricReader(ConfigProperties config) {
    PrometheusHttpServerBuilder prometheusBuilder = PrometheusHttpServer.builder();

    Integer port = config.getInt("otel.exporter.prometheus.port");
    if (port != null) {
      prometheusBuilder.setPort(port);
    }
    String host = config.getString("otel.exporter.prometheus.host");
    if (host != null) {
      prometheusBuilder.setHost(host);
    }

    ExporterBuilderUtil.configureExporterMemoryMode(config, prometheusBuilder::setMemoryMode);

    return prometheusBuilder.build();
  }

  @Override
  public String getName() {
    return "prometheus";
  }
}
