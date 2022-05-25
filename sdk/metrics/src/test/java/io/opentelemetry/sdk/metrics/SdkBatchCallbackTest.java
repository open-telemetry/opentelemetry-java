/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BatchCallback;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableDoubleMeasurement;
import io.opentelemetry.api.metrics.ObservableLongMeasurement;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.state.SdkObservableMeasurement;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.event.Level;

class SdkBatchCallbackTest {

  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkBatchCallbackTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .registerMetricReader(sdkMeterReader)
          .setResource(RESOURCE)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @RegisterExtension
  LogCapturer observableMeasurementLogs =
      LogCapturer.create().forLevel(Level.TRACE).captureForType(SdkObservableMeasurement.class);

  @RegisterExtension LogCapturer sdkMeterLogs = LogCapturer.create().captureForType(SdkMeter.class);

  @Test
  void collectAllMetrics() {
    ObservableLongMeasurement longCounter = sdkMeter.counterBuilder("longCounter").buildObserver();
    ObservableDoubleMeasurement doubleCounter =
        sdkMeter.counterBuilder("doubleCounter").ofDoubles().buildObserver();
    ObservableLongMeasurement upDownlongCounter =
        sdkMeter.upDownCounterBuilder("longUpDownCounter").buildObserver();
    ObservableDoubleMeasurement upDowndoubleCounter =
        sdkMeter.upDownCounterBuilder("doubleUpDownCounter").ofDoubles().buildObserver();
    ObservableLongMeasurement longGauge =
        sdkMeter.gaugeBuilder("longGauge").ofLongs().buildObserver();
    ObservableDoubleMeasurement doubleGauge = sdkMeter.gaugeBuilder("doubleGauge").buildObserver();

    BatchCallback batchCallback =
        sdkMeter.batchCallback(
            () -> {
              longCounter.record(1, Attributes.builder().put("key", "val1").build());
              doubleCounter.record(1.1, Attributes.builder().put("key", "val2").build());
              upDownlongCounter.record(2, Attributes.builder().put("key", "val3").build());
              upDowndoubleCounter.record(2.2, Attributes.builder().put("key", "val4").build());
              longGauge.record(3, Attributes.builder().put("key", "val5").build());
              doubleGauge.record(3.3, Attributes.builder().put("key", "val6").build());
            },
            longCounter,
            doubleCounter,
            upDownlongCounter,
            upDowndoubleCounter,
            longGauge,
            doubleGauge);

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("longCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(1)
                                            .hasAttributes(attributeEntry("key", "val1")))),
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("doubleCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(1.1)
                                            .hasAttributes(attributeEntry("key", "val2")))),
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("longUpDownCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(2)
                                            .hasAttributes(attributeEntry("key", "val3")))),
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("doubleUpDownCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(2.2)
                                            .hasAttributes(attributeEntry("key", "val4")))),
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("longGauge")
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(3)
                                        .hasAttributes(attributeEntry("key", "val5")))),
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("doubleGauge")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(3.3)
                                        .hasAttributes(attributeEntry("key", "val6")))));

    // Should not be called after closed
    batchCallback.close();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectAllMetrics_RecordToUnregisteredInstrument() {
    ObservableLongMeasurement counter1 = sdkMeter.counterBuilder("counter1").buildObserver();
    ObservableLongMeasurement counter2 = sdkMeter.counterBuilder("counter2").buildObserver();

    sdkMeter.batchCallback(
        () -> {
          counter1.record(1);
          counter2.record(1);
        },
        counter1);

    // counter2 is not registered and measurements should be ignored
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum -> sum.isMonotonic().hasPointsSatisfying(point -> point.hasValue(1))));

    observableMeasurementLogs.assertContains(
        "Measurement recorded for instrument counter2 outside callback registered to instrument. Dropping measurement.");
  }

  @Test
  void collectAllMetrics_RecordOutsideCallback() {
    ObservableLongMeasurement counter1 = sdkMeter.counterBuilder("counter1").buildObserver();

    // Recordings outside of callbacks are ignored
    counter1.record(1, Attributes.builder().put("key", "value1").build());

    sdkMeter.batchCallback(
        () -> counter1.record(1, Attributes.builder().put("key", "value2").build()), counter1);

    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(1)
                                            .hasAttributes(attributeEntry("key", "value2")))));

    observableMeasurementLogs.assertContains(
        "Measurement recorded for instrument counter1 outside callback registered to instrument. Dropping measurement.");
  }

  @Test
  @SuppressLogger(SdkMeter.class)
  void collectAllMetrics_InstrumentFromAnotherMeter() {
    ObservableLongMeasurement counter1 = sdkMeter.counterBuilder("counter1").buildObserver();
    ObservableLongMeasurement counter2 =
        sdkMeterProvider.get("another-meter").counterBuilder("counter2").buildObserver();

    sdkMeter.batchCallback(
        () -> {
          counter1.record(1);
          counter2.record(2);
        },
        counter1,
        counter2);

    // counter2 belongs to another meter and measurements should be ignored
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum -> sum.isMonotonic().hasPointsSatisfying(point -> point.hasValue(1))));

    sdkMeterLogs.assertContains(
        "batchCallback called with instruments that belong to a different Meter.");
  }

  @Test
  @SuppressLogger(SdkMeter.class)
  void collectAllMetrics_InvalidObserver() {
    ObservableLongMeasurement counter1 = sdkMeter.counterBuilder("counter1").buildObserver();
    ObservableLongMeasurement counter2 =
        new ObservableLongMeasurement() {
          @Override
          public void record(long value) {}

          @Override
          public void record(long value, Attributes attributes) {}
        };

    sdkMeter.batchCallback(
        () -> {
          counter1.record(1);
          counter2.record(2);
        },
        counter1,
        counter2);

    // counter2 was not obtained from the sdk and its measurements should be ignored.
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("counter1")
                    .hasLongSumSatisfying(
                        sum -> sum.isMonotonic().hasPointsSatisfying(point -> point.hasValue(1))));

    sdkMeterLogs.assertContains(
        "batchCallback called with instruments that were not created by the SDK.");
  }
}
