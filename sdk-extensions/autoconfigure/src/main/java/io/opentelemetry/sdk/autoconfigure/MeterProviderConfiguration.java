/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import io.opentelemetry.sdk.metrics.internal.SdkMeterProviderUtil;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarFilter;
import java.util.function.BiFunction;

final class MeterProviderConfiguration {

  static void configureMeterProvider(
      SdkMeterProviderBuilder meterProviderBuilder,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super MetricExporter, ConfigProperties, ? extends MetricExporter>
          metricExporterCustomizer) {

    // Configure default exemplar filters.
    String exemplarFilter = config.getString("otel.metrics.exemplar.filter");
    if (exemplarFilter == null) {
      exemplarFilter = "with_sampled_trace";
    }
    switch (exemplarFilter) {
      case "none":
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.neverSample());
        break;
      case "all":
        SdkMeterProviderUtil.setExemplarFilter(meterProviderBuilder, ExemplarFilter.alwaysSample());
        break;
      case "with_sampled_trace":
      default:
        SdkMeterProviderUtil.setExemplarFilter(
            meterProviderBuilder, ExemplarFilter.sampleWithTraces());
        break;
    }

    String exporterName = config.getString("otel.metrics.exporter");
    if (exporterName == null) {
      exporterName = "otlp";
    }
    if (!exporterName.equals("none")) {
      MetricExporterConfiguration.configureExporter(
          exporterName, config, serviceClassLoader, meterProviderBuilder, metricExporterCustomizer);
    }
  }

  private MeterProviderConfiguration() {}
}
