/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.google.common.testing.GcFinalization;
import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.descriptor.Advice;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.state.CallbackRegistration;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

class SdkObservableInstrumentTest {

  @RegisterExtension
  LogCapturer logs = LogCapturer.create().captureForType(SdkObservableInstrument.class);

  private CallbackRegistration callbackRegistration;
  private SdkMeter sdkMeter;
  private SdkObservableInstrument observableInstrument;

  @BeforeEach
  void setup() {
    sdkMeter = mock(SdkMeter.class);
    callbackRegistration =
        CallbackRegistration.create(
            Collections.singletonList(
                SdkObservableMeasurement.create(
                    InstrumentationScopeInfo.create("meter"),
                    InstrumentDescriptor.create(
                        "my-instrument",
                        "description",
                        "unit",
                        InstrumentType.COUNTER,
                        InstrumentValueType.DOUBLE,
                        Advice.empty()),
                    Collections.emptyList())),
            () -> {});

    observableInstrument = new SdkObservableInstrument(sdkMeter, callbackRegistration);
  }

  @Test
  @SuppressLogger(SdkObservableInstrument.class)
  void close() {
    // First call to close should trigger remove from meter shared state
    observableInstrument.close();
    verify(sdkMeter).removeCallback(callbackRegistration);
    logs.assertDoesNotContain("has called close() multiple times.");

    // Close a second time should not trigger remove from meter shared state
    Mockito.reset(sdkMeter);
    observableInstrument.close();
    verify(sdkMeter, never()).removeCallback(callbackRegistration);
    logs.assertContains(
        "CallbackRegistration{instrumentDescriptors=[InstrumentDescriptor{name=my-instrument");
    logs.assertContains("has called close() multiple times.");
  }

  @Test
  @SuppressLogger(SdkObservableInstrument.class)
  void close_releasesCallback() {
    InMemoryMetricReader reader = InMemoryMetricReader.create();
    try (SdkMeterProvider meterProvider =
        SdkMeterProvider.builder().registerMetricReader(reader).build()) {
      AtomicLong value = new AtomicLong();
      Consumer<ObservableLongMeasurement> callback =
          measurement -> measurement.record(value.incrementAndGet());
      ObservableLongGauge gauge =
          meterProvider.get("meter").gaugeBuilder("my-gauge").ofLongs().buildWithCallback(callback);
      WeakReference<Consumer<ObservableLongMeasurement>> callbackReference =
          new WeakReference<>(callback);
      callback = null;

      assertThat(reader.collectAllMetrics()).hasSize(1);
      gauge.close();
      GcFinalization.awaitClear(callbackReference);
      assertThat(reader.collectAllMetrics()).isEmpty();

      // Keep the gauge reachable through the GC check.
      gauge.close();
    }
  }

  @Test
  void stringRepresentation() {
    String expected =
        "SdkObservableInstrument{"
            + "callback=CallbackRegistration{"
            + "instrumentDescriptors=["
            + "InstrumentDescriptor{"
            + "name=my-instrument, "
            + "description=description, "
            + "unit=unit, "
            + "type=COUNTER, "
            + "valueType=DOUBLE, "
            + "advice=Advice{explicitBucketBoundaries=null, attributes=null}}"
            + "]}}";
    assertThat(observableInstrument.toString()).isEqualTo(expected);

    observableInstrument.close();
    assertThat(observableInstrument.toString()).isEqualTo(expected);
  }
}
