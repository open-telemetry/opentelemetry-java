/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.metrics;

import static com.google.common.truth.Truth.assertThat;

import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.common.Labels;
import io.opentelemetry.metrics.LongValueRecorder;
import io.opentelemetry.metrics.LongValueRecorder.BoundLongValueRecorder;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link LongValueRecorderSdk}. */
@RunWith(JUnit4.class)
public class LongValueRecorderSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Attributes.of("resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(
          "io.opentelemetry.sdk.metrics.LongValueRecorderSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void collectMetrics_NoRecords() {
    LongValueRecorderSdk longMeasure =
        testSdk
            .longValueRecorderBuilder("testMeasure")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("ms")
            .build();
    assertThat(longMeasure).isInstanceOf(LongValueRecorderSdk.class);
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList)
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testMeasure",
                    "My very own counter",
                    "ms",
                    Type.SUMMARY,
                    Labels.of("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>emptyList()));
  }

  @Test
  public void collectMetrics_emptyCollectionCycle() {
    LongValueRecorderSdk longMeasure =
        testSdk
            .longValueRecorderBuilder("testMeasure")
            .setConstantLabels(Labels.of("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("ms")
            .build();

    longMeasure.bind("key", "value");
    testClock.advanceNanos(SECOND_NANOS);

    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList)
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testMeasure",
                    "My very own counter",
                    "ms",
                    Type.SUMMARY,
                    Labels.of("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>emptyList()));
  }

  @Test
  public void collectMetrics_WithOneRecord() {
    LongValueRecorderSdk longMeasure = testSdk.longValueRecorderBuilder("testMeasure").build();
    testClock.advanceNanos(SECOND_NANOS);
    longMeasure.record(12);
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList)
        .containsExactly(
            MetricData.create(
                Descriptor.create("testMeasure", "", "1", Type.SUMMARY, Labels.empty()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    SummaryPoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Labels.empty(),
                        1,
                        12,
                        valueAtPercentiles(12, 12)))));
  }

  @Test
  public void collectMetrics_WithMultipleCollects() {
    Labels labelSet = Labels.of("K", "V");
    Labels emptyLabelSet = Labels.empty();
    long startTime = testClock.now();
    LongValueRecorderSdk longMeasure = testSdk.longValueRecorderBuilder("testMeasure").build();
    BoundLongValueRecorder boundMeasure = longMeasure.bind("K", "V");
    try {
      // Do some records using bounds and direct calls and bindings.
      longMeasure.record(12);
      boundMeasure.record(123);
      longMeasure.record(-14);
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      boundMeasure.record(321);
      longMeasure.record(-121, "K", "V");

      long firstCollect = testClock.now();
      List<MetricData> metricDataList = longMeasure.collectAll();
      assertThat(metricDataList).hasSize(1);
      MetricData metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              SummaryPoint.create(
                  startTime, firstCollect, emptyLabelSet, 2, -2, valueAtPercentiles(-14, 12)),
              SummaryPoint.create(
                  startTime, firstCollect, labelSet, 3, 323, valueAtPercentiles(-121, 321)));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      boundMeasure.record(222);
      longMeasure.record(17);

      long secondCollect = testClock.now();
      metricDataList = longMeasure.collectAll();
      assertThat(metricDataList).hasSize(1);
      metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              SummaryPoint.create(
                  startTime, secondCollect, emptyLabelSet, 3, 15, valueAtPercentiles(-14, 17)),
              SummaryPoint.create(
                  startTime, secondCollect, labelSet, 4, 545, valueAtPercentiles(-121, 321)));
    } finally {
      boundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet() {
    LongValueRecorderSdk longMeasure = testSdk.longValueRecorderBuilder("testMeasure").build();
    BoundLongValueRecorder boundMeasure = longMeasure.bind("K", "v");
    BoundLongValueRecorder duplicateBoundMeasure = longMeasure.bind("K", "v");
    try {
      assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
    } finally {
      boundMeasure.unbind();
      duplicateBoundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    LongValueRecorderSdk longMeasure = testSdk.longValueRecorderBuilder("testMeasure").build();
    BoundLongValueRecorder boundMeasure = longMeasure.bind("K", "v");
    try {
      longMeasure.collectAll();
      BoundLongValueRecorder duplicateBoundMeasure = longMeasure.bind("K", "v");
      try {
        assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
      } finally {
        duplicateBoundMeasure.unbind();
      }
    } finally {
      boundMeasure.unbind();
    }
  }

  @Test
  public void stressTest() {
    final LongValueRecorderSdk longMeasure =
        testSdk.longValueRecorderBuilder("testMeasure").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longMeasure).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new LongValueRecorderSdkTest.OperationUpdaterDirectCall(longMeasure, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new LongValueRecorderSdkTest.OperationUpdaterWithBinding(
                  longMeasure.bind("K", "V"))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Labels.of("K", "V"),
                16_000,
                160_000,
                valueAtPercentiles(9, 11)));
  }

  @Test
  public void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final LongValueRecorderSdk longMeasure =
        testSdk.longValueRecorderBuilder("testMeasure").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longMeasure).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new LongValueRecorderSdkTest.OperationUpdaterDirectCall(
                  longMeasure, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new LongValueRecorderSdkTest.OperationUpdaterWithBinding(
                  longMeasure.bind(keys[i], values[i]))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Labels.of(keys[0], values[0]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Labels.of(keys[1], values[1]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Labels.of(keys[2], values[2]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Labels.of(keys[3], values[3]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundLongValueRecorder boundLongValueRecorder;

    private OperationUpdaterWithBinding(BoundLongValueRecorder boundLongValueRecorder) {
      this.boundLongValueRecorder = boundLongValueRecorder;
    }

    @Override
    void update() {
      boundLongValueRecorder.record(9);
    }

    @Override
    void cleanup() {
      boundLongValueRecorder.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final LongValueRecorder longValueRecorder;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(
        LongValueRecorder longValueRecorder, String key, String value) {
      this.longValueRecorder = longValueRecorder;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longValueRecorder.record(11, key, value);
    }

    @Override
    void cleanup() {}
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
