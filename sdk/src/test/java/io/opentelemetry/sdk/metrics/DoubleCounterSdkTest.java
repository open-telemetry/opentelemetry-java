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
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor.Type;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.List;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DoubleCounterSdk}. */
@RunWith(JUnit4.class)
public class DoubleCounterSdkTest {

  @Rule public ExpectedException thrown = ExpectedException.none();
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(
          Collections.singletonMap(
              "resource_key", AttributeValue.stringAttributeValue("resource_value")));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.DoubleCounterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void collectMetrics_NoRecords() {
    DoubleCounterSdk doubleCounter =
        testSdk
            .doubleCounterBuilder("testCounter")
            .setConstantLabels(Collections.singletonMap("sk1", "sv1"))
            .setDescription("My very own counter")
            .setUnit("ms")
            .setMonotonic(true)
            .build();
    List<MetricData> metricDataList = doubleCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getDescriptor())
        .isEqualTo(
            Descriptor.create(
                "testCounter",
                "My very own counter",
                "ms",
                Type.MONOTONIC_DOUBLE,
                Collections.singletonMap("sk1", "sv1")));
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).isEmpty();
  }

  @Test
  public void collectMetrics_WithOneRecord() {
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();
    testClock.advanceNanos(SECOND_NANOS);
    doubleCounter.add(12.1d);
    List<MetricData> metricDataList = doubleCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).hasSize(1);
    // TODO: This is not perfect because we compare double values using direct equal, maybe worth
    //  changing to do a proper comparison for double values, here and everywhere else.
    assertThat(metricData.getPoints())
        .containsExactly(
            DoublePoint.create(
                testClock.now() - SECOND_NANOS,
                testClock.now(),
                Collections.<String, String>emptyMap(),
                12.1d));
  }

  @Test
  public void collectMetrics_WithMultipleCollects() {
    LabelSetSdk labelSet = LabelSetSdk.create("K", "V");
    LabelSetSdk emptyLabelSet = LabelSetSdk.create();
    long startTime = testClock.now();
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter boundCounter = doubleCounter.bind("K", "V");
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleCounter.add(12.1d);
      boundCounter.add(123.3d);
      doubleCounter.add(21.4d);
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      boundCounter.add(321.5d);
      doubleCounter.add(111.1d, "K", "V");

      long firstCollect = testClock.now();
      List<MetricData> metricDataList = doubleCounter.collectAll();
      assertThat(metricDataList).hasSize(1);
      MetricData metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              DoublePoint.create(startTime, firstCollect, labelSet.getLabels(), 555.9d),
              DoublePoint.create(startTime, firstCollect, emptyLabelSet.getLabels(), 33.5d));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      boundCounter.add(222d);
      doubleCounter.add(11d);

      long secondCollect = testClock.now();
      metricDataList = doubleCounter.collectAll();
      assertThat(metricDataList).hasSize(1);
      metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              DoublePoint.create(startTime, secondCollect, labelSet.getLabels(), 777.9d),
              DoublePoint.create(startTime, secondCollect, emptyLabelSet.getLabels(), 44.5d));
    } finally {
      boundCounter.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet() {
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter boundCounter = doubleCounter.bind("K", "v");
    BoundDoubleCounter duplicateBoundCounter = doubleCounter.bind("K", "v");
    try {
      assertThat(duplicateBoundCounter).isEqualTo(boundCounter);
    } finally {
      boundCounter.unbind();
      duplicateBoundCounter.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter boundCounter = doubleCounter.bind("K", "v");
    try {
      doubleCounter.collectAll();
      BoundDoubleCounter duplicateBoundCounter = doubleCounter.bind("K", "v");
      try {
        assertThat(duplicateBoundCounter).isEqualTo(boundCounter);
      } finally {
        duplicateBoundCounter.unbind();
      }
    } finally {
      boundCounter.unbind();
    }
  }

  @Test
  public void doubleCounterAdd_Monotonicity() {
    DoubleCounterSdk doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.add(-45.77d);
  }

  @Test
  public void boundDoubleCounterAdd_Monotonicity() {
    DoubleCounterSdk doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.bind().add(-9.3);
  }

  @Test
  public void stressTest() {
    final DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(doubleCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterWithBinding(doubleCounter.bind("K", "V"))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = doubleCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            DoublePoint.create(
                testClock.now(), testClock.now(), Collections.singletonMap("K", "V"), 80_000));
  }

  @Test
  public void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleCounterSdk doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(doubleCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterWithBinding(doubleCounter.bind(keys[i], values[i]))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = doubleCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            DoublePoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[0], values[0]),
                40_000),
            DoublePoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[1], values[1]),
                40_000),
            DoublePoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[2], values[2]),
                40_000),
            DoublePoint.create(
                testClock.now(),
                testClock.now(),
                Collections.singletonMap(keys[3], values[3]),
                40_000));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final DoubleCounter.BoundDoubleCounter boundDoubleCounter;

    private OperationUpdaterWithBinding(BoundDoubleCounter boundDoubleCounter) {
      this.boundDoubleCounter = boundDoubleCounter;
    }

    @Override
    void update() {
      boundDoubleCounter.add(9.0);
    }

    @Override
    void cleanup() {
      boundDoubleCounter.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final DoubleCounter doubleCounter;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(DoubleCounter doubleCounter, String key, String value) {
      this.doubleCounter = doubleCounter;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleCounter.add(11.0, key, value);
    }

    @Override
    void cleanup() {}
  }
}
