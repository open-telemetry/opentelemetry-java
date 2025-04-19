/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class ExporterMetricsTest {

  @SuppressWarnings("unchecked")
  Supplier<MeterProvider> meterProviderSupplier = mock(Supplier.class);

  @Test
  void createHttpProtobuf_validMeterProvider() {
    when(meterProviderSupplier.get())
        .thenReturn(
            SdkMeterProvider.builder()
                // Have to provide a valid reader.
                .registerMetricReader(
                    new MetricReader() {
                      @Override
                      public void register(CollectionRegistration registration) {}

                      @Override
                      public CompletableResultCode forceFlush() {
                        return CompletableResultCode.ofSuccess();
                      }

                      @Override
                      public CompletableResultCode shutdown() {
                        return CompletableResultCode.ofSuccess();
                      }

                      @Override
                      public AggregationTemporality getAggregationTemporality(
                          InstrumentType instrumentType) {
                        return AggregationTemporality.CUMULATIVE;
                      }
                    })
                .build());
    ExporterMetrics exporterMetrics =
        ExporterMetrics.createHttpProtobuf("test", "test", meterProviderSupplier);
    verifyNoInteractions(meterProviderSupplier); // Ensure lazy

    // Verify the supplier is only called once per underlying meter.
    exporterMetrics.addSeen(10);
    exporterMetrics.addSeen(20);
    verify(meterProviderSupplier, times(1)).get();
    exporterMetrics.addSuccess(30);
    exporterMetrics.addSuccess(40);
    verify(meterProviderSupplier, times(2)).get();
    exporterMetrics.addFailed(50);
    exporterMetrics.addFailed(60);
    verify(meterProviderSupplier, times(2)).get();
  }

  @Test
  void createHttpProtobuf_noopMeterProvider() {
    when(meterProviderSupplier.get()).thenReturn(MeterProvider.noop());
    ExporterMetrics exporterMetrics =
        ExporterMetrics.createHttpProtobuf("test", "test", meterProviderSupplier);
    verifyNoInteractions(meterProviderSupplier); // Ensure lazy

    // Verify the supplier is invoked multiple times since it returns a noop meter.
    exporterMetrics.addSeen(10);
    exporterMetrics.addSeen(20);
    verify(meterProviderSupplier, times(2)).get();
    exporterMetrics.addSuccess(30);
    exporterMetrics.addSuccess(40);
    verify(meterProviderSupplier, times(4)).get();
    exporterMetrics.addFailed(50);
    exporterMetrics.addFailed(60);
    verify(meterProviderSupplier, times(6)).get();
  }
}
