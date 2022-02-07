/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizer;
import io.opentelemetry.sdk.autoconfigure.spi.AutoConfigurationCustomizerProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.stream.Collectors;

public class MetricExporterCustomizer implements AutoConfigurationCustomizerProvider {
  @Override
  public void customize(AutoConfigurationCustomizer autoConfiguration) {
    autoConfiguration.addMetricExporterCustomizer(
        (delegate, config) ->
            new MetricExporter() {
              @Override
              public CompletableResultCode export(Collection<MetricData> metrics) {
                // Note: this is for testing purposes only. If you wish to filter metrics by name
                // please configure the SdkMeterProvider with the appropriate view.
                Collection<MetricData> filtered =
                    metrics.stream()
                        .filter(metricData -> metricData.getName().equals("my-metric"))
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
            });
  }
}
