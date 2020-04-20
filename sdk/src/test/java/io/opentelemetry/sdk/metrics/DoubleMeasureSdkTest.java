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
import io.opentelemetry.metrics.DoubleMeasure;
import io.opentelemetry.metrics.DoubleMeasure.BoundDoubleMeasure;
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

/** Unit tests for {@link DoubleMeasureSdk}. */
@RunWith(JUnit4.class)
public class DoubleMeasureSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.DoubleMeasureSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void collectMetrics_NoRecords() {
    DoubleMeasureSdk doubleMeasure =
        testSdk
            .doubleMeasureBuilder("testMeasure")
            .setConstantLabels(Collections.singletonMap("sk1", "sv1"))
            .setDescription("My very own measure")
            .setUnit("ms")
            .build();
    assertThat(doubleMeasure).isInstanceOf(DoubleMeasureSdk.class);
    List<MetricData> metricDataList = doubleMeasure.collectAll();
    assertThat(metricDataList)
        .containsExactly(
            MetricData.create(
                Descriptor.create(
                    "testMeasure",
                    "My very own measure",
                    "ms",
                    Type.SUMMARY,
                    Collections.singletonMap("sk1", "sv1")),
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                Collections.<Point>emptyList()));
  }

  @Test
  public void collectMetrics_WithOneRecord() {
    DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();
    testClock.advanceNanos(SECOND_NANOS);
    doubleMeasure.record(12.1d);
    List<MetricData> metricDataList = doubleMeasure.collectAll();
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
                        12.1d,
                        valueAtPercentiles(12.1d, 12.1d)))));
  }

  @Test
  public void collectMetrics_WithMultipleCollects() {
    LabelSetSdk labelSet = LabelSetSdk.create("K", "V");
    LabelSetSdk emptyLabelSet = LabelSetSdk.create();
    long startTime = testClock.now();
    DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();
    BoundDoubleMeasure boundMeasure = doubleMeasure.bind("K", "V");
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleMeasure.record(12.1d);
      boundMeasure.record(123.3d);
      doubleMeasure.record(21.4d);
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      boundMeasure.record(321.5d);
      doubleMeasure.record(111.1d, "K", "V");

      long firstCollect = testClock.now();
      List<MetricData> metricDataList = doubleMeasure.collectAll();
      assertThat(metricDataList).hasSize(1);
      MetricData metricData = metricDataList.get(0);
      assertThat(metricData.getPoints())
          .containsExactly(
              SummaryPoint.create(
                  startTime,
                  firstCollect,
                  emptyLabelSet.getLabels(),
                  2,
                  33.5d,
                  valueAtPercentiles(12.1d, 21.4d)),
              SummaryPoint.create(
                  startTime,
                  firstCollect,
                  labelSet.getLabels(),
                  3,
                  555.9d,
                  valueAtPercentiles(111.1d, 321.5d)));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      boundMeasure.record(222d);
      doubleMeasure.record(11d);

      long secondCollect = testClock.now();
      metricDataList = doubleMeasure.collectAll();
      assertThat(metricDataList).hasSize(1);
      metricData = metricDataList.get(0);
      assertThat(metricData.getPoints())
          .containsExactly(
              SummaryPoint.create(
                  startTime,
                  secondCollect,
                  emptyLabelSet.getLabels(),
                  3,
                  44.5d,
                  valueAtPercentiles(11d, 21.4d)),
              SummaryPoint.create(
                  startTime,
                  secondCollect,
                  labelSet.getLabels(),
                  4,
                  777.9d,
                  valueAtPercentiles(111.1d, 321.5d)));
    } finally {
      boundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet() {
    DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();
    BoundDoubleMeasure boundMeasure = doubleMeasure.bind("K", "v");
    BoundDoubleMeasure duplicateBoundMeasure = doubleMeasure.bind("K", "v");
    try {
      assertThat(duplicateBoundMeasure).isEqualTo(boundMeasure);
    } finally {
      boundMeasure.unbind();
      duplicateBoundMeasure.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();
    BoundDoubleMeasure boundMeasure = doubleMeasure.bind("K", "v");
    try {
      doubleMeasure.collectAll();
      BoundDoubleMeasure duplicateBoundMeasure = doubleMeasure.bind("K", "v");
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
  public void doubleMeasureRecord_Absolute() {
    DoubleMeasureSdk doubleMeasure =
        testSdk.doubleMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleMeasure.record(-45.77d);
  }

  @Test
  public void boundDoubleMeasureRecord_Absolute() {
    DoubleMeasureSdk doubleMeasure =
        testSdk.doubleMeasureBuilder("testMeasure").setAbsolute(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleMeasure.bind().record(-9.3f);
  }

  @Test
  public void stressTest() {
    final DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleMeasure).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new DoubleMeasureSdkTest.OperationUpdaterDirectCall(doubleMeasure, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterWithBinding(doubleMeasure.bind("K", "V"))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = doubleMeasure.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap("K", "V"),
                8_000,
                80_000,
                valueAtPercentiles(9.0, 11.0)));
  }

  @Test
  public void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleMeasureSdk doubleMeasure = testSdk.doubleMeasureBuilder("testMeasure").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleMeasure).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new DoubleMeasureSdkTest.OperationUpdaterDirectCall(
                  doubleMeasure, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterWithBinding(doubleMeasure.bind(keys[i], values[i]))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = doubleMeasure.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[0], values[0]),
                4_000,
                40_000d,
                valueAtPercentiles(9.0, 11.0)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[1], values[1]),
                4_000,
                40_000d,
                valueAtPercentiles(9.0, 11.0)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[2], values[2]),
                4_000,
                40_000d,
                valueAtPercentiles(9.0, 11.0)),
            SummaryPoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[3], values[3]),
                4_000,
                40_000d,
                valueAtPercentiles(9.0, 11.0)));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final DoubleMeasure.BoundDoubleMeasure boundDoubleMeasure;

    private OperationUpdaterWithBinding(BoundDoubleMeasure boundDoubleMeasure) {
      this.boundDoubleMeasure = boundDoubleMeasure;
    }

    @Override
    void update() {
      boundDoubleMeasure.record(11.0);
    }

    @Override
    void cleanup() {
      boundDoubleMeasure.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {
    private final DoubleMeasure doubleMeasure;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(DoubleMeasure doubleMeasure, String key, String value) {
      this.doubleMeasure = doubleMeasure;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleMeasure.record(9.0, key, value);
    }

    @Override
    void cleanup() {}
  }

  private static List<ValueAtPercentile> valueAtPercentiles(double min, double max) {
    return Arrays.asList(ValueAtPercentile.create(0, min), ValueAtPercentile.create(100, max));
  }
}
