/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.metrics;

import static io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData.createDoubleGauge;
import static java.util.Collections.singleton;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounterBuilder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.SdkMeterProvider;
import io.opentelemetry.sdk.metrics.SdkMeterProviderBuilder;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.PeriodicMetricReader;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import org.junit.jupiter.api.Test;

class OtlpHttpMetricExporterBuilderTest {

  private static final Collection<MetricData> DATA_SET =
      singleton(
          createDoubleGauge(
              Resource.empty(),
              InstrumentationScopeInfo.create("test"),
              "test",
              "test",
              "test",
              ImmutableGaugeData.empty()));

  private final SdkMeterProvider meterProvider = mock(SdkMeterProvider.class);
  private final Meter meter = mock(Meter.class);
  private final LongCounterBuilder counterBuilder = mock(LongCounterBuilder.class);
  private final LongCounter counter = mock(LongCounter.class);

  @Test
  void setMeterProvider_null() {
    OtlpHttpMetricExporterBuilder builder = OtlpHttpMetricExporter.builder();
    assertThrows(
        NullPointerException.class,
        () -> builder.setMeterProvider((MeterProvider) null),
        "MeterProvider must not be null");
    assertThrows(
        NullPointerException.class,
        () -> builder.setMeterProvider((Supplier<MeterProvider>) null),
        "MeterProvider must not be null");
  }

  @Test
  void setMeterProvider() {
    when(meterProvider.get(any())).thenReturn(meter);
    when(meter.counterBuilder(eq("otlp.exporter.seen"))).thenReturn(counterBuilder);
    when(counterBuilder.build()).thenReturn(counter);

    @SuppressWarnings("unchecked")
    Supplier<MeterProvider> provider = mock(Supplier.class);
    try (OtlpHttpMetricExporter exporter =
        OtlpHttpMetricExporter.builder().setMeterProvider(provider).build()) {
      verifyNoInteractions(provider, meterProvider, meter, counterBuilder, counter);

      // Collection before MeterProvider is initialized.
      when(provider.get()).thenReturn(MeterProvider.noop());
      exporter.export(DATA_SET);

      verifyNoInteractions(meterProvider, meter, counterBuilder, counter);

      // Collection after MeterProvider is initialized.
      when(provider.get()).thenReturn(meterProvider);
      exporter.export(DATA_SET);

      verify(meter).counterBuilder(eq("otlp.exporter.seen"));
      verify(counter).add(eq(1L), any());
      verifyNoMoreInteractions(meter, counter);
    }
  }

  @Test
  void setMeterProvider_defaultGlobal() {
    GlobalOpenTelemetry.set(
        new OpenTelemetry() {
          @Override
          public MeterProvider getMeterProvider() {
            return meterProvider;
          }

          @Override
          public TracerProvider getTracerProvider() {
            return TracerProvider.noop();
          }

          @Override
          public ContextPropagators getPropagators() {
            return ContextPropagators.noop();
          }
        });
    when(meterProvider.get(any())).thenReturn(meter);
    when(meter.counterBuilder(eq("otlp.exporter.seen"))).thenReturn(counterBuilder);
    when(counterBuilder.build()).thenReturn(counter);

    try (OtlpHttpMetricExporter exporter = OtlpHttpMetricExporter.builder().build()) {
      verifyNoInteractions(meterProvider, meter, counterBuilder, counter);

      exporter.export(DATA_SET);

      verify(meter).counterBuilder(eq("otlp.exporter.seen"));
      verify(counter).add(eq(1L), any());
      verifyNoMoreInteractions(meter, counter);
    } finally {
      GlobalOpenTelemetry.resetForTest();
    }
  }

  @Test
  void setMeterProvider_noMocks() {
    AtomicReference<SdkMeterProvider> meterProviderAtomicReference = new AtomicReference<>();
    SdkMeterProviderBuilder builder =
        SdkMeterProvider.builder()
            .registerMetricReader(
                PeriodicMetricReader.create(
                    OtlpHttpMetricExporter.builder()
                        .setMeterProvider(meterProviderAtomicReference::get)
                        .build()));
    meterProviderAtomicReference.set(builder.build());
    meterProviderAtomicReference.get().close();
  }
}
