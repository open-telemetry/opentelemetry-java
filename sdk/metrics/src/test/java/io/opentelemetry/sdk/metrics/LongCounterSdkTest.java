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
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.LongCounter.BoundLongCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongCounterSdk}. */
class LongCounterSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(LongCounterSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final SdkMeter testSdk = sdkMeterProvider.get(getClass().getName());

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(() -> testSdk.longCounterBuilder("testCounter").build().add(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> testSdk.longCounterBuilder("testCounter").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    BoundLongCounter bound = longCounter.bind(Labels.of("foo", "bar"));
    try {
      assertThat(testSdk.collectAll(testClock.now())).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabels() {
    LongCounterSdk longCounter =
        testSdk
            .longCounterBuilder("testCounter")
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    longCounter.add(12, Labels.empty());
    longCounter.add(12);
    assertThat(testSdk.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testCounter",
                "description",
                "By",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now() - SECOND_NANOS,
                            testClock.now(),
                            Labels.empty(),
                            24)))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();
    BoundLongCounter bound = longCounter.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      longCounter.add(12, Labels.empty());
      bound.add(123);
      longCounter.add(21, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(321);
      longCounter.add(111, Labels.of("K", "V"));
      assertThat(testSdk.collectAll(testClock.now()))
          .containsExactly(
              MetricData.createLongSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testCounter",
                  "",
                  "1",
                  MetricData.LongSumData.create(
                      /* isMonotonic= */ true,
                      MetricData.AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          MetricData.LongPoint.create(
                              startTime, testClock.now(), Labels.of("K", "V"), 555),
                          MetricData.LongPoint.create(
                              startTime, testClock.now(), Labels.empty(), 33)))));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(222);
      longCounter.add(11, Labels.empty());
      assertThat(testSdk.collectAll(testClock.now()))
          .containsExactly(
              MetricData.createLongSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testCounter",
                  "",
                  "1",
                  MetricData.LongSumData.create(
                      /* isMonotonic= */ true,
                      MetricData.AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          MetricData.LongPoint.create(
                              startTime, testClock.now(), Labels.of("K", "V"), 777),
                          MetricData.LongPoint.create(
                              startTime, testClock.now(), Labels.empty(), 44)))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void longCounterAdd_MonotonicityCheck() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();

    assertThatThrownBy(() -> longCounter.add(-45, Labels.empty()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void boundLongCounterAdd_MonotonicityCheck() {
    LongCounterSdk longCounter = testSdk.longCounterBuilder("testCounter").build();

    assertThatThrownBy(() -> longCounter.bind(Labels.empty()).add(-9))
        .isInstanceOf(IllegalArgumentException.class);
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
    assertThat(testSdk.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.LongPoint.create(
                            testClock.now(), testClock.now(), Labels.of("K", "V"), 160_000)))));
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
    assertThat(testSdk.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testCounter",
                "",
                "1",
                MetricData.LongSumData.create(
                    /* isMonotonic= */ true,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Arrays.asList(
                        MetricData.LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[0], values[0]),
                            20_000),
                        MetricData.LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[1], values[1]),
                            20_000),
                        MetricData.LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[2], values[2]),
                            20_000),
                        MetricData.LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[3], values[3]),
                            20_000)))));
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
