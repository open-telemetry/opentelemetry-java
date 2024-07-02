/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.metrics.export.MetricReader;

/**
 * File configuration SPI implementation for {@link PrometheusHttpServer}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class PrometheusComponentProvider implements ComponentProvider<MetricReader> {

  @Override
  public Class<MetricReader> getType() {
    return MetricReader.class;
  }

  @Override
  public String getName() {
    return "prometheus";
  }

  @Override
  public MetricReader create(StructuredConfigProperties config) {
    PrometheusHttpServerBuilder prometheusBuilder = PrometheusHttpServer.builder();

    Integer port = config.getInt("port");
    if (port != null) {
      prometheusBuilder.setPort(port);
    }
    String host = config.getString("host");
    if (host != null) {
      prometheusBuilder.setHost(host);
    }
    String defaultHistogramAggregation = config.getString("default_histogram_aggregation");
    if (defaultHistogramAggregation != null) {
      ExporterBuilderUtil.configureHistogramDefaultAggregation(
          defaultHistogramAggregation, prometheusBuilder::setDefaultAggregationSelector);
    }

    return prometheusBuilder.build();
  }
}
