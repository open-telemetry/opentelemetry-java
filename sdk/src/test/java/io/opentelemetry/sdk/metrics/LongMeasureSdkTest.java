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
import io.opentelemetry.metrics.LongMeasure;
import io.opentelemetry.metrics.LongMeasure.BoundLongMeasure;
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

/** Unit tests for {@link LongMeasureSdk}. */
@RunWith(JUnit4.class)
public class LongMeasureSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.LongMeasureSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void collectMetrics_NoRecords() {
    LongMeasureSdk longMeasure =
        testSdk
            .longMeasureBuilder("testMeasure")
            .setConstantLabels(Collections.singletonMap("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("ms")
            .setAbsolute(true)
            .build();
    assertThat(longMeasure).isInstanceOf(LongMeasureSdk.class);
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList)
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testMeasure",
                    "My very own counter",
                    "ms",
                    Type.SUMMARY,
                    Collections.singletonMap("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>emptyList()));
  }

  @Test
  public void collectMetrics_WithOneRecord() {
    LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testMeasure").build();
    testClock.advanceNanos(SECOND_NANOS);
    longMeasure.record(12);
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList)
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testMeasure", "", "1", Type.SUMMARY, Collections.<String, String>emptyMap()),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>singletonList(
                    SummaryPoint.create(
                        testClock.now() - SECOND_NANOS,
                        testClock.now(),
                        Collections.<String, String>emptyMap(),
                        1,
                        12,
                        valueAtPercentiles(12, 12)))));
  }

  @Test
  public void collectMetrics_WithMultipleCollects() {
    LabelSetSdk labelSet = LabelSetSdk.create("K", "V");
    LabelSetSdk emptyLabelSet = LabelSetSdk.create();
    long startTime = testClock.now();
    LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testMeasure").build();
    BoundLongMeasure boundMeasure = longMeasure.bind("K", "V");
    try {
      // Do some records using bounds and direct calls and bindings.
      longMeasure.record(12);
      boundMeasure.record(123);
      longMeasure.record(21);
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      boundMeasure.record(321);
      longMeasure.record(111, "K", "V");

      long firstCollect = testClock.now();
      List<MetricData> metricDataList = longMeasure.collectAll();
      assertThat(metricDataList).hasSize(1);
      MetricData metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              SummaryPoint.create(
                  startTime,
                  firstCollect,
                  emptyLabelSet.getLabels(),
                  2,
                  33,
                  valueAtPercentiles(12, 21)),
              SummaryPoint.create(
                  startTime,
                  firstCollect,
                  labelSet.getLabels(),
                  3,
                  555,
                  valueAtPercentiles(111, 321)));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      boundMeasure.record(222);
      longMeasure.record(11);

      long secondCollect = testClock.now();
      metricDataList = longMeasure.collectAll();
      assertThat(metricDataList).hasSize(1);
      metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              SummaryPoint.create(
                  startTime,
                  secondCollect,
                  emptyLabelSet.getLabels(),
                  3,
                  44,
                  valueAtPercentiles(11, 21)),
              SummaryPoint.create(
                  startTime,
                  secondCollect,
                  labelSet.getLabels(),
                  4,
                  777,
                  valueAtPercentiles(111, 321)));
    } finally {
      boundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet() {
    LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testMeasure").build();
    BoundLongMeasure boundMeasure = longMeasure.bind("K", "v");
    BoundLongMeasure duplicateBoundMeasure = longMeasure.bind("K", "v");
    try {
      assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
    } finally {
      boundMeasure.unbind();
      duplicateBoundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testMeasure").build();
    BoundLongMeasure boundMeasure = longMeasure.bind("K", "v");
    try {
      longMeasure.collectAll();
      BoundLongMeasure duplicateBoundMeasure = longMeasure.bind("K", "v");
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
  public void longMeasureRecord_Absolute() {
    LongMeasureSdk longMeasure =
        testSdk.longMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    longMeasure.record(-45);
  }

  @Test
  public void boundLongMeasure_Absolute() {
    LongMeasureSdk longMeasure =
        testSdk.longMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    longMeasure.bind().record(-9);
  }

  @Test
  public void stressTest() {
    final LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testMeasure").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longMeasure).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new LongMeasureSdkTest.OperationUpdaterDirectCall(longMeasure, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new LongMeasureSdkTest.OperationUpdaterWithBinding(longMeasure.bind("K", "V"))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = longMeasure.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap("K", "V"),
                16_000,
                160_000,
                valueAtPercentiles(9, 11)));
  }

  @Test
  public void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final LongMeasureSdk longMeasure = testSdk.longMeasureBuilder("testMeasure").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longMeasure).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new LongMeasureSdkTest.OperationUpdaterDirectCall(longMeasure, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new LongMeasureSdkTest.OperationUpdaterWithBinding(
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
                Collections.singletonMap(keys[0], values[0]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[1], values[1]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[2], values[2]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[3], values[3]),
                2_000,
                20_000,
                valueAtPercentiles(9, 11)));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final LongMeasure.BoundLongMeasure boundLongMeasure;

    private OperationUpdaterWithBinding(BoundLongMeasure boundLongMeasure) {
      this.boundLongMeasure = boundLongMeasure;
    }

    @Override
    void update() {
      boundLongMeasure.record(9);
    }

    @Override
    void cleanup() {
      boundLongMeasure.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final LongMeasure longMeasure;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(LongMeasure longMeasure, String key, String value) {
      this.longMeasure = longMeasure;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longMeasure.record(11, key, value);
    }

    @Override
    void cleanup() {}
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
