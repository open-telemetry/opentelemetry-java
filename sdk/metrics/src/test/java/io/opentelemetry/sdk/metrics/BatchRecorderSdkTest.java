/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleValueRecorder;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongValueRecorder;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link BatchRecorderSdk}. */
class BatchRecorderSdkTest {
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(BatchRecorderSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  private static final Attributes attributeSet = Attributes.builder().put("key", "value").build();

  @Test
  void batchRecorder_badLabelSet() {
    assertThatThrownBy(() -> sdkMeter.newBatchRecorder("key").record())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("key/value");
  }

  @Test
  void batchRecorder() {
    DoubleCounter doubleCounter = sdkMeter.doubleCounterBuilder("testDoubleCounter").build();
    LongCounter longCounter = sdkMeter.longCounterBuilder("testLongCounter").build();
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build();
    LongUpDownCounter longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testLongUpDownCounter").build();
    DoubleValueRecorder doubleValueRecorder =
        sdkMeter.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    LongValueRecorder longValueRecorder =
        sdkMeter.longValueRecorderBuilder("testLongValueRecorder").build();

    BatchRecorder batchRecorder = sdkMeter.newBatchRecorder("key", "value");

    batchRecorder
        .put(longCounter, 12)
        .put(doubleUpDownCounter, -12.1d)
        .put(longUpDownCounter, -12)
        .put(doubleCounter, 12.1d)
        .put(doubleCounter, 12.1d)
        .put(longValueRecorder, 13)
        .put(doubleValueRecorder, 13.1d);

    // until record() is called, nothing should be recorded.
    Collection<MetricData> preRecord = sdkMeterProvider.collectAllMetrics();
    preRecord.forEach(metricData -> assertThat(metricData.isEmpty()).isTrue());

    batchRecorder.record();

    assertBatchRecordings(
        doubleCounter,
        longCounter,
        doubleUpDownCounter,
        longUpDownCounter,
        doubleValueRecorder,
        longValueRecorder,
        /* shouldHaveDeltas=*/ true);

    // a second record, with no recordings added should not change any of the values.
    batchRecorder.record();
    assertBatchRecordings(
        doubleCounter,
        longCounter,
        doubleUpDownCounter,
        longUpDownCounter,
        doubleValueRecorder,
        longValueRecorder,
        /* shouldHaveDeltas=*/ false);
  }

  @SuppressWarnings("unchecked")
  private void assertBatchRecordings(
      DoubleCounter doubleCounter,
      LongCounter longCounter,
      DoubleUpDownCounter doubleUpDownCounter,
      LongUpDownCounter longUpDownCounter,
      DoubleValueRecorder doubleValueRecorder,
      LongValueRecorder longValueRecorder,
      boolean shouldHaveDeltas) {
    assertThat(((AbstractInstrument) doubleCounter).collectAll(testClock.now()))
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testDoubleCounter")
                    .hasDoubleSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasEpochNanos(testClock.now())
                                .hasStartEpochNanos(testClock.now())
                                .hasValue(24.2d)
                                .hasAttributes(attributeSet)));
    assertThat(((AbstractInstrument) longCounter).collectAll(testClock.now()))
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testLongCounter")
                    .hasLongSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasEpochNanos(testClock.now())
                                .hasStartEpochNanos(testClock.now())
                                .hasValue(12)
                                .hasAttributes(attributeSet)));
    assertThat(((AbstractInstrument) doubleUpDownCounter).collectAll(testClock.now()))
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testDoubleUpDownCounter")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasEpochNanos(testClock.now())
                                .hasStartEpochNanos(testClock.now())
                                .hasValue(-12.1)
                                .hasAttributes(attributeSet)));
    assertThat(((AbstractInstrument) longUpDownCounter).collectAll(testClock.now()))
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testLongUpDownCounter")
                    .hasLongSum()
                    .isNotMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasEpochNanos(testClock.now())
                                .hasStartEpochNanos(testClock.now())
                                .hasValue(-12)
                                .hasAttributes(attributeSet)));

    if (shouldHaveDeltas) {
      assertThat(((AbstractInstrument) doubleValueRecorder).collectAll(testClock.now()))
          .containsExactly(
              MetricData.createDoubleSummary(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testDoubleValueRecorder",
                  "",
                  "1",
                  DoubleSummaryData.create(
                      Collections.singletonList(
                          DoubleSummaryPointData.create(
                              testClock.now(),
                              testClock.now(),
                              attributeSet,
                              1,
                              13.1d,
                              Arrays.asList(
                                  ValueAtPercentile.create(0.0, 13.1),
                                  ValueAtPercentile.create(100.0, 13.1)))))));
    } else {
      assertThat(((AbstractInstrument) doubleValueRecorder).collectAll(testClock.now())).isEmpty();
    }

    if (shouldHaveDeltas) {
      assertThat(((AbstractInstrument) longValueRecorder).collectAll(testClock.now()))
          .containsExactly(
              MetricData.createDoubleSummary(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testLongValueRecorder",
                  "",
                  "1",
                  DoubleSummaryData.create(
                      Collections.singletonList(
                          DoubleSummaryPointData.create(
                              testClock.now(),
                              testClock.now(),
                              attributeSet,
                              1,
                              13,
                              Arrays.asList(
                                  ValueAtPercentile.create(0.0, 13),
                                  ValueAtPercentile.create(100.0, 13)))))));
    } else {
      assertThat(((AbstractInstrument) longValueRecorder).collectAll(testClock.now())).isEmpty();
    }
  }
}
