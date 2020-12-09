/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.LongCounterSdk.BoundInstrument;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import org.assertj.core.api.recursive.comparison.RecursiveComparisonConfiguration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongCounterSdk}. */
class LongCounterSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create("io.opentelemetry.sdk.metrics.LongCounterSdkTest", null);
  private final TestClock testClock = TestClock.create();
  private final MeterProviderSharedState meterProviderSharedState =
      MeterProviderSharedState.create(testClock, RESOURCE);
  private final MeterSdk testSdk =
      new MeterSdk(meterProviderSharedState, INSTRUMENTATION_LIBRARY_INFO, new ViewRegistry());

  @Test
  void add_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> testSdk.longCounterBuilder("testCounter").build().add(1, null),
        "labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThrows(
        NullPointerException.class,
        () -> testSdk.longCounterBuilder("testCounter").build().bind(null),
        "labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    LongCounterSdk longCounter =
        testSdk
            .longCounterBuilder("testCounter")
            .setDescription("My very own counter")
            .setUnit("ms")
            .build();
    List<MetricData> metricDataList = longCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getName()).isEqualTo("testCounter");
    assertThat(metricData.getDescription()).isEqualTo("My very own counter");
    assertThat(metricData.getUnit()).isEqualTo("ms");
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_SUM);
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).isEmpty();
  }

  @Test
  void collectMetrics_bound_NoRecords() {
    LongCounterSdk longCounter =
        testSdk
            .longCounterBuilder("testCounter")
            .setDescription("My very own counter")
            .setUnit("ms")
            .build();
    BoundInstrument ignored = longCounter.bind(Labels.of("foo", "bar"));

    List<MetricData> metricDataList = longCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getName()).isEqualTo("testCounter");
    assertThat(metricData.getDescription()).isEqualTo("My very own counter");
    assertThat(metricData.getUnit()).isEqualTo("ms");
    assertThat(metricData.getType()).isEqualTo(MetricData.Type.LONG_SUM);
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).isEmpty();
  }

  @Test
  void collectMetrics_WithOneRecord() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    testClock.advanceNanos(SECOND_NANOS);
    longCounter.add(12, Labels.empty());
    List<MetricData> metricDataList = longCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    MetricData metricData = metricDataList.get(0);
    assertThat(metricData.getResource()).isEqualTo(RESOURCE);
    assertThat(metricData.getInstrumentationLibraryInfo()).isEqualTo(INSTRUMENTATION_LIBRARY_INFO);
    assertThat(metricData.getPoints()).hasSize(1);
    assertThat(metricData.getPoints())
        .containsExactly(
            LongPoint.create(testClock.now() - SECOND_NANOS, testClock.now(), Labels.empty(), 12));
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    LongCounterSdk longCounter1 = testSdk.longCounterBuilder("testCounter1").build();
    testClock.advanceNanos(SECOND_NANOS);
    longCounter.add(12, Labels.empty());
    longCounter1.add(12);

    assertThat(longCounter.collectAll().get(0))
        .usingRecursiveComparison(
            RecursiveComparisonConfiguration.builder().withIgnoredFields("name").build())
        .isEqualTo(longCounter1.collectAll().get(0));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    BoundLongCounter boundCounter = longCounter.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      longCounter.add(12, Labels.empty());
      boundCounter.add(123);
      longCounter.add(21, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      boundCounter.add(321);
      longCounter.add(111, Labels.of("K", "V"));

      long firstCollect = testClock.now();
      List<MetricData> metricDataList = longCounter.collectAll();
      assertThat(metricDataList).hasSize(1);
      MetricData metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              LongPoint.create(startTime, firstCollect, Labels.of("K", "V"), 555),
              LongPoint.create(startTime, firstCollect, Labels.empty(), 33));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      boundCounter.add(222);
      longCounter.add(11, Labels.empty());

      long secondCollect = testClock.now();
      metricDataList = longCounter.collectAll();
      assertThat(metricDataList).hasSize(1);
      metricData = metricDataList.get(0);
      assertThat(metricData.getPoints()).hasSize(2);
      assertThat(metricData.getPoints())
          .containsExactly(
              LongPoint.create(startTime, secondCollect, Labels.of("K", "V"), 777),
              LongPoint.create(startTime, secondCollect, Labels.empty(), 44));
    } finally {
      boundCounter.unbind();
    }
  }

  @Test
  void sameBound_ForSameLabelSet() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    BoundLongCounter boundCounter = longCounter.bind(Labels.of("K", "V"));
    BoundLongCounter duplicateBoundCounter = longCounter.bind(Labels.of("K", "V"));
    try {
      assertThat(duplicateBoundCounter).isEqualTo(boundCounter);
    } finally {
      boundCounter.unbind();
      duplicateBoundCounter.unbind();
    }
  }

  @Test
  void sameBound_ForSameLabelSet_InDifferentCollectionCycles() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    BoundLongCounter boundCounter = longCounter.bind(Labels.of("K", "V"));
    try {
      longCounter.collectAll();
      BoundLongCounter duplicateBoundCounter = longCounter.bind(Labels.of("K", "V"));
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
  void longCounterAdd_MonotonicityCheck() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();

    assertThrows(IllegalArgumentException.class, () -> longCounter.add(-45, Labels.empty()));
  }

  @Test
  void boundLongCounterAdd_MonotonicityCheck() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();

    assertThrows(IllegalArgumentException.class, () -> longCounter.bind(Labels.empty()).add(-9));
  }

  @Test
  void stressTest() {
    final LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(longCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterWithBinding(longCounter.bind(Labels.of("K", "V")))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = longCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            LongPoint.create(testClock.now(), testClock.now(), Labels.of("K", "V"), 160_000));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(longCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(longCounter.bind(Labels.of(keys[i], values[i])))));
    }

    stressTestBuilder.build().run();
    List<MetricData> metricDataList = longCounter.collectAll();
    assertThat(metricDataList).hasSize(1);
    assertThat(metricDataList.get(0).getPoints())
        .containsExactly(
            LongPoint.create(
                testClock.now(), testClock.now(), Labels.of(keys[0], values[0]), 20_000),
            LongPoint.create(
                testClock.now(), testClock.now(), Labels.of(keys[1], values[1]), 20_000),
            LongPoint.create(
                testClock.now(), testClock.now(), Labels.of(keys[2], values[2]), 20_000),
            LongPoint.create(
                testClock.now(), testClock.now(), Labels.of(keys[3], values[3]), 20_000));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final LongCounter.BoundLongCounter boundLongCounter;

    private OperationUpdaterWithBinding(BoundLongCounter boundLongCounter) {
      this.boundLongCounter = boundLongCounter;
    }

    @Override
    void update() {
      boundLongCounter.add(9);
    }

    @Override
    void cleanup() {
      boundLongCounter.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final LongCounter longCounter;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(LongCounter longCounter, String key, String value) {
      this.longCounter = longCounter;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longCounter.add(11, Labels.of(key, value));
    }

    @Override
    void cleanup() {}
  }
}
