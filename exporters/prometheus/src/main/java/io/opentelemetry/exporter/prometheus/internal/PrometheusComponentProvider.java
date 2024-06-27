/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus.internal;

import static io.opentelemetry.sdk.metrics.Aggregation.explicitBucketHistogram;

import io.opentelemetry.exporter.prometheus.PrometheusHttpServer;
import io.opentelemetry.exporter.prometheus.PrometheusHttpServerBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.StructuredConfigProperties;
import io.opentelemetry.sdk.metrics.Aggregation;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import io.opentelemetry.sdk.metrics.internal.aggregator.AggregationUtil;

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
    String defaultHistogramAggregation = config.getString("metrics.default.histogram.aggregation");
    if (defaultHistogramAggregation != null) {
      if (AggregationUtil.aggregationName(Aggregation.base2ExponentialBucketHistogram())
          .equalsIgnoreCase(defaultHistogramAggregation)) {
        prometheusBuilder.setDefaultAggregationSelector(
            DefaultAggregationSelector.getDefault()
                .with(InstrumentType.HISTOGRAM, Aggregation.base2ExponentialBucketHistogram()));
      } else if (!AggregationUtil.aggregationName(explicitBucketHistogram())
          .equalsIgnoreCase(defaultHistogramAggregation)) {
        throw new ConfigurationException(
            "Unrecognized default histogram aggregation: " + defaultHistogramAggregation);
      }
    }

    return prometheusBuilder.build();
  }
}
