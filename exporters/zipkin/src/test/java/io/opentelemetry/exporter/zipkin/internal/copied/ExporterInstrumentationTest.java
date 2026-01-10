/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.zipkin.internal.copied;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.export.CollectionRegistration;
import io.opentelemetry.sdk.metrics.export.MetricReader;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.Mockito;

class ExporterInstrumentationTest {

  @SuppressWarnings("unchecked")
  Supplier<MeterProvider> meterProviderSupplier = mock(Supplier.class);

  @ParameterizedTest
  @EnumSource
  void validMeterProvider(InternalTelemetryVersion schemaVersion) {
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
    ExporterInstrumentation instrumentation =
        new ExporterInstrumentation(
            schemaVersion,
            meterProviderSupplier,
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER),
            "http://testing:1234");
    verifyNoInteractions(meterProviderSupplier); // Ensure lazy

    // Verify the supplier is only called once per underlying meter.

    instrumentation.startRecordingExport(42).finishFailed("foo");
    instrumentation.startRecordingExport(42).finishSuccessful();
    verify(meterProviderSupplier, atLeastOnce()).get();

    instrumentation.startRecordingExport(42).finishFailed("foo");
    instrumentation.startRecordingExport(42).finishSuccessful();
    verifyNoMoreInteractions(meterProviderSupplier);
  }

  @ParameterizedTest
  @EnumSource
  void noopMeterProvider(InternalTelemetryVersion schemaVersion) {

    when(meterProviderSupplier.get()).thenReturn(MeterProvider.noop());
    ExporterInstrumentation instrumentation =
        new ExporterInstrumentation(
            schemaVersion,
            meterProviderSupplier,
            ComponentId.generateLazy(StandardComponentId.ExporterType.OTLP_GRPC_SPAN_EXPORTER),
            "http://testing:1234");
    verifyNoInteractions(meterProviderSupplier); // Ensure lazy

    // Verify the supplier is invoked multiple times since it returns a noop meter.
    instrumentation.startRecordingExport(42).finishFailed("foo");
    instrumentation.startRecordingExport(42).finishSuccessful();
    verify(meterProviderSupplier, atLeastOnce()).get();

    Mockito.clearInvocations((Object) meterProviderSupplier);
    instrumentation.startRecordingExport(42).finishFailed("foo");
    instrumentation.startRecordingExport(42).finishSuccessful();
    verify(meterProviderSupplier, atLeastOnce()).get();
  }

  @Test
  void serverAttributesInvalidUrl() {
    assertThat(ExporterInstrumentation.extractServerAttributes("^")).isEmpty();
  }

  @Test
  void serverAttributesEmptyUrl() {
    assertThat(ExporterInstrumentation.extractServerAttributes("")).isEmpty();
  }

  @Test
  void serverAttributesHttps() {
    assertThat(ExporterInstrumentation.extractServerAttributes("https://example.com/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 443);

    assertThat(
            ExporterInstrumentation.extractServerAttributes("https://example.com:1234/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 1234);
  }

  @Test
  void serverAttributesHttp() {
    assertThat(ExporterInstrumentation.extractServerAttributes("http://example.com/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 80);

    assertThat(
            ExporterInstrumentation.extractServerAttributes("http://example.com:1234/foo/bar?a=b"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "example.com")
        .containsEntry(SemConvAttributes.SERVER_PORT, 1234);
  }

  @Test
  void serverAttributesUnknownScheme() {
    assertThat(ExporterInstrumentation.extractServerAttributes("custom://foo"))
        .hasSize(1)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "foo");

    assertThat(ExporterInstrumentation.extractServerAttributes("custom://foo:1234"))
        .hasSize(2)
        .containsEntry(SemConvAttributes.SERVER_ADDRESS, "foo")
        .containsEntry(SemConvAttributes.SERVER_PORT, 1234);
  }
}
