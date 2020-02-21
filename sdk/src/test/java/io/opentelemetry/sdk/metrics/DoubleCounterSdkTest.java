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

import com.google.common.collect.ImmutableMap;
import io.opentelemetry.metrics.DoubleCounter;
import io.opentelemetry.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.data.MetricData;
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
      Resource.create(Collections.singletonMap("resource_key", "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.DoubleCounterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO);

  @Test
  public void collectMetrics_NoRecords() {
    DoubleCounter doubleCounter =
        testSdk
            .doubleCounterBuilder("testCounter")
            .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
            .setLabelKeys(Collections.singletonList("sk1"))
            .setDescription("My very own counter")
            .setUnit("metric tonnes")
            .setMonotonic(true)
            .build();
    assertThat(doubleCounter).isInstanceOf(DoubleCounterSdk.class);
    List<MetricData> metricDataList = ((DoubleCounterSdk) doubleCounter).collect();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).isEmpty();
  }

  @Test
  public void collectMetrics_WithOneRecord() {
    DoubleCounterSdk doubleCounter =
        (DoubleCounterSdk)
            testSdk
                .doubleCounterBuilder("testCounter")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setMonotonic(true)
                .build();
    testClock.advanceNanos(SECOND_NANOS);
    doubleCounter.add(12.1d, testSdk.createLabelSet());
    List<MetricData> metricDataList = doubleCounter.collect();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).hasSize(1);
    // TODO: This is not perfect because we compare double values using direct equal, maybe worth
    //  changing to do a proper comparison for double values, here and everywhere in this file.
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
    LabelSetSdk labelSet = testSdk.createLabelSet("K", "V");
    LabelSetSdk emptyLabelSet = testSdk.createLabelSet();
    long startTime = testClock.now();
    DoubleCounterSdk doubleCounter =
        (DoubleCounterSdk)
            testSdk
                .doubleCounterBuilder("testCounter")
                .setConstantLabels(ImmutableMap.of("sk1", "sv1"))
                .setLabelKeys(Collections.singletonList("sk1"))
                .setDescription("My very own counter")
                .setUnit("metric tonnes")
                .setMonotonic(true)
                .build();
    BoundDoubleCounter boundCounter = doubleCounter.bind(labelSet);
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleCounter.add(12.1d, emptyLabelSet);
      boundCounter.add(123.3d);
      doubleCounter.add(21.4d, emptyLabelSet);
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      boundCounter.add(321.5d);
      doubleCounter.add(111.1d, labelSet);

      long firstCollect = testClock.now();
      List<MetricData> metricDataList = doubleCounter.collect();
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
      doubleCounter.add(11d, emptyLabelSet);

      long secondCollect = testClock.now();
      metricDataList = doubleCounter.collect();
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
    DoubleCounter doubleCounter = testSdk.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter boundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    BoundDoubleCounter duplicateBoundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    try {
      assertThat(duplicateBoundCounter).isEqualTo(boundCounter);
    } finally {
      boundCounter.unbind();
      duplicateBoundCounter.unbind();
    }
  }

  @Test
  public void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    DoubleCounterSdk doubleCounter =
        (DoubleCounterSdk) testSdk.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter boundCounter = doubleCounter.bind(testSdk.createLabelSet("K", "v"));
    try {
      doubleCounter.collect();
      BoundDoubleCounter duplicateBoundCounter =
          doubleCounter.bind(testSdk.createLabelSet("K", "v"));
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
    DoubleCounter doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.add(-45.77d, testSdk.createLabelSet());
  }

  @Test
  public void boundDoubleCounterAdd_Monotonicity() {
    DoubleCounter doubleCounter =
        testSdk.doubleCounterBuilder("testCounter").setMonotonic(true).build();

    thrown.expect(IllegalArgumentException.class);
    doubleCounter.bind(testSdk.createLabelSet()).add(-9.3);
  }
}
