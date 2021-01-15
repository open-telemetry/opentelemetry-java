/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.BatchRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
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
  private final SdkMeter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void batchRecorder_badLabelSet() {
    assertThatThrownBy(() -> sdkMeter.newBatchRecorder("key").record())
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessageContaining("key/value");
  }

  @Test
  void batchRecorder() {
    DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testDoubleCounter").build();
    LongCounterSdk longCounter = sdkMeter.longCounterBuilder("testLongCounter").build();
    DoubleUpDownCounterSdk doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testDoubleUpDownCounter").build();
    LongUpDownCounterSdk longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testLongUpDownCounter").build();
    DoubleValueRecorderSdk doubleValueRecorder =
        sdkMeter.doubleValueRecorderBuilder("testDoubleValueRecorder").build();
    LongValueRecorderSdk longValueRecorder =
        sdkMeter.longValueRecorderBuilder("testLongValueRecorder").build();
    Labels labelSet = Labels.of("key", "value");

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
        labelSet,
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
        labelSet,
        /* shouldHaveDeltas=*/ false);
  }

  private void assertBatchRecordings(
      DoubleCounterSdk doubleCounter,
      LongCounterSdk longCounter,
      DoubleUpDownCounterSdk doubleUpDownCounter,
      LongUpDownCounterSdk longUpDownCounter,
      DoubleValueRecorderSdk doubleValueRecorder,
      LongValueRecorderSdk longValueRecorder,
      Labels labelSet,
      boolean shouldHaveDeltas) {
    assertThat(doubleCounter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleCounter",
                "",
                "1",
                DoubleSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        DoublePointData.create(
                            testClock.now(), testClock.now(), labelSet, 24.2d)))));
    assertThat(longCounter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongCounter",
                "",
                "1",
                LongSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPointData.create(testClock.now(), testClock.now(), labelSet, 12)))));
    assertThat(doubleUpDownCounter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testDoubleUpDownCounter",
                "",
                "1",
                DoubleSumData.create(
                    /* isMonotonic= */ false,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        DoublePointData.create(
                            testClock.now(), testClock.now(), labelSet, -12.1d)))));
    assertThat(longUpDownCounter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testLongUpDownCounter",
                "",
                "1",
                LongSumData.create(
                    /* isMonotonic= */ false,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPointData.create(testClock.now(), testClock.now(), labelSet, -12)))));

    if (shouldHaveDeltas) {
      assertThat(doubleValueRecorder.collectAll(testClock.now()))
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
                              labelSet,
                              1,
                              13.1d,
                              Arrays.asList(
                                  ValueAtPercentile.create(0.0, 13.1),
                                  ValueAtPercentile.create(100.0, 13.1)))))));
    } else {
      assertThat(doubleValueRecorder.collectAll(testClock.now())).isEmpty();
    }

    if (shouldHaveDeltas) {
      assertThat(longValueRecorder.collectAll(testClock.now()))
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
                              labelSet,
                              1,
                              13,
                              Arrays.asList(
                                  ValueAtPercentile.create(0.0, 13),
                                  ValueAtPercentile.create(100.0, 13)))))));
    } else {
      assertThat(longValueRecorder.collectAll(testClock.now())).isEmpty();
    }
  }
}
