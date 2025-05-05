/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.metrics;

import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetrySchemaVersion;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.function.Supplier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class ExporterInstrumentationTest {

  @SuppressWarnings("unchecked")
  Supplier<MeterProvider> meterProviderSupplier = mock(Supplier.class);

  @ParameterizedTest
  @EnumSource()
  void validMeterProvider(InternalTelemetrySchemaVersion schemaVersion) {
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
    ExporterInstrumentation instrumentation = new ExporterInstrumentation(schemaVersion, meterProviderSupplier, ExporterMetrics.Signal.SPAN, ComponentId.generateLazy("test"), null, "test", "test");
    verifyNoInteractions(meterProviderSupplier); // Ensure lazy

    // Verify the supplier is only called once per underlying meter.

    instrumentation.startRecordingExport(42).finishFailed("foo", Attributes.empty());
    instrumentation.startRecordingExport(42).finishSuccessful(Attributes.empty());
    if (schemaVersion == InternalTelemetrySchemaVersion.OFF) {
      verifyNoInteractions(meterProviderSupplier);
    } else {
      verify(meterProviderSupplier, atLeastOnce()).get();
    }

    instrumentation.startRecordingExport(42).finishFailed("foo", Attributes.empty());
    instrumentation.startRecordingExport(42).finishSuccessful(Attributes.empty());
    verifyNoMoreInteractions(meterProviderSupplier);
  }


  @ParameterizedTest
  @EnumSource()
  void noopMeterProvider(InternalTelemetrySchemaVersion schemaVersion) {
    if (schemaVersion == InternalTelemetrySchemaVersion.OFF) {
      return; // Nothing to test for No-Op
    }

    when(meterProviderSupplier.get()).thenReturn(MeterProvider.noop());
    ExporterInstrumentation instrumentation = new ExporterInstrumentation(schemaVersion, meterProviderSupplier, ExporterMetrics.Signal.SPAN, ComponentId.generateLazy("test"), null, "test", "test");
    verifyNoInteractions(meterProviderSupplier); // Ensure lazy

    // Verify the supplier is invoked multiple times since it returns a noop meter.
    instrumentation.startRecordingExport(42).finishFailed("foo", Attributes.empty());
    instrumentation.startRecordingExport(42).finishSuccessful(Attributes.empty());
    verify(meterProviderSupplier, atLeastOnce()).get();

    Mockito.clearInvocations((Object) meterProviderSupplier);
    instrumentation.startRecordingExport(42).finishFailed("foo", Attributes.empty());
    instrumentation.startRecordingExport(42).finishSuccessful(Attributes.empty());
    verify(meterProviderSupplier, atLeastOnce()).get();
  }
}
