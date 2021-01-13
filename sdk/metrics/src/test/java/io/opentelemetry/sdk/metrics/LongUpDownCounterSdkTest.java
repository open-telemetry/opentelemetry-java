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
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.LongUpDownCounter.BoundLongUpDownCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongPoint;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link LongUpDownCounterSdk}. */
class LongUpDownCounterSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(LongUpDownCounterSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final SdkMeter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.longUpDownCounterBuilder("testCounter").build().add(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.longUpDownCounterBuilder("testUpDownCounter").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    LongUpDownCounterSdk longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testUpDownCounter").build();
    BoundLongUpDownCounter bound = longUpDownCounter.bind(Labels.of("foo", "bar"));
    try {
      assertThat(sdkMeterProvider.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    LongUpDownCounterSdk longUpDownCounter =
        sdkMeter
            .longUpDownCounterBuilder("testUpDownCounter")
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    longUpDownCounter.add(12, Labels.empty());
    longUpDownCounter.add(12);
    assertThat(sdkMeterProvider.collectAllMetrics())
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testUpDownCounter",
                "description",
                "By",
                LongSumData.create(
                    /* isMonotonic= */ false,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(
                            testClock.now() - SECOND_NANOS,
                            testClock.now(),
                            Labels.empty(),
                            24)))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongUpDownCounterSdk longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testUpDownCounter").build();
    BoundLongUpDownCounter bound = longUpDownCounter.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      longUpDownCounter.add(12, Labels.empty());
      bound.add(123);
      longUpDownCounter.add(21, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(321);
      longUpDownCounter.add(111, Labels.of("K", "V"));
      assertThat(sdkMeterProvider.collectAllMetrics())
          .containsExactly(
              MetricData.createLongSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testUpDownCounter",
                  "",
                  "1",
                  LongSumData.create(
                      /* isMonotonic= */ false,
                      AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          LongPoint.create(startTime, testClock.now(), Labels.of("K", "V"), 555),
                          LongPoint.create(startTime, testClock.now(), Labels.empty(), 33)))));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(222);
      longUpDownCounter.add(11, Labels.empty());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .containsExactly(
              MetricData.createLongSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testUpDownCounter",
                  "",
                  "1",
                  LongSumData.create(
                      /* isMonotonic= */ false,
                      AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          LongPoint.create(startTime, testClock.now(), Labels.of("K", "V"), 777),
                          LongPoint.create(startTime, testClock.now(), Labels.empty(), 44)))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final LongUpDownCounterSdk longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testUpDownCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longUpDownCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(longUpDownCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(longUpDownCounter.bind(Labels.of("K", "V")))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterProvider.collectAllMetrics())
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testUpDownCounter",
                "",
                "1",
                LongSumData.create(
                    /* isMonotonic= */ false,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        LongPoint.create(
                            testClock.now(), testClock.now(), Labels.of("K", "V"), 160_000)))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final LongUpDownCounterSdk longUpDownCounter =
        sdkMeter.longUpDownCounterBuilder("testUpDownCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(longUpDownCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(longUpDownCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(
                  longUpDownCounter.bind(Labels.of(keys[i], values[i])))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterProvider.collectAllMetrics())
        .containsExactly(
            MetricData.createLongSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testUpDownCounter",
                "",
                "1",
                LongSumData.create(
                    /* isMonotonic= */ false,
                    AggregationTemporality.CUMULATIVE,
                    Arrays.asList(
                        LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[0], values[0]),
                            20_000),
                        LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[1], values[1]),
                            20_000),
                        LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[2], values[2]),
                            20_000),
                        LongPoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[3], values[3]),
                            20_000)))));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundLongUpDownCounter boundLongUpDownCounter;

    private OperationUpdaterWithBinding(BoundLongUpDownCounter boundLongUpDownCounter) {
      this.boundLongUpDownCounter = boundLongUpDownCounter;
    }

    @Override
    void update() {
      boundLongUpDownCounter.add(9);
    }

    @Override
    void cleanup() {
      boundLongUpDownCounter.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final LongUpDownCounter longUpDownCounter;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(
        LongUpDownCounter longUpDownCounter, String key, String value) {
      this.longUpDownCounter = longUpDownCounter;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      longUpDownCounter.add(11, Labels.of(key, value));
    }

    @Override
    void cleanup() {}
  }
}
