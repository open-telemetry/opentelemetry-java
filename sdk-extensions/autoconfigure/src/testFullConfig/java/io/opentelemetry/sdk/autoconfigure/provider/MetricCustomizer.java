/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentSelector;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.View;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.stream.Collectors;

/** Behavior asserted in {@link io.opentelemetry.sdk.autoconfigure.FullConfigTest}. */
public class MetricCustomizer implements AutoConfigurationCustomizerProvider {
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addMeterProviderCustomizer(MetricCustomizer::sdkMeterProviderCustomizer);
    autoConfiguration.addMetricExporterCustomizer(MetricCustomizer::exporterCustomizer);
  }

  private static SdkMeterProviderBuilder sdkMeterProviderCustomizer(
      SdkMeterProviderBuilder meterProviderBuilder, ConfigProperties configProperties) {
    meterProviderBuilder.registerView(
        InstrumentSelector.builder().setName("my-metric").build(),
        View.builder().setAttributeFilter(name -> name.equals("allowed")).build());
    return meterProviderBuilder;
  }

  private static MetricExporter exporterCustomizer(
      MetricExporter delegate, ConfigProperties config) {
    return new MetricExporter() {
      @Override
      public AggregationTemporality getAggregationTemporality(InstrumentType instrumentType) {
        return AggregationTemporality.CUMULATIVE;
      }

      @Override
      public CompletableResultCode export(Collection<MetricData> metrics) {
        // Note: this is for testing purposes only. If you wish to filter metrics by name
        // please configure the SdkMeterProvider with the appropriate view.
        Collection<MetricData> filtered =
            metrics.stream()
                .filter(
                    metricData ->
                        metricData.getName().equals("my-metric")
                            || metricData
                                .getInstrumentationScopeInfo()
                                .getName()
                                .startsWith("io.opentelemetry.exporters"))
                .collect(Collectors.toList());
        return delegate.export(filtered);
      }

      @Override
      public CompletableResultCode flush() {
        return delegate.flush();
      }

      @Override
      public CompletableResultCode shutdown() {
        return delegate.shutdown();
      }
    };
  }
}
