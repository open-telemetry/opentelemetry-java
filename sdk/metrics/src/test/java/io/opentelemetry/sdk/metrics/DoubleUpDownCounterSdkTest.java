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
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.DoubleUpDownCounter.BoundDoubleUpDownCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleUpDownCounterSdk}. */
class DoubleUpDownCounterSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(DoubleUpDownCounterSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final SdkMeter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.doubleUpDownCounterBuilder("testUpDownCounter").build().add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.doubleUpDownCounterBuilder("testUpDownCounter").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleUpDownCounterSdk doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testUpDownCounter").build();
    BoundDoubleUpDownCounter bound = doubleUpDownCounter.bind(Labels.of("foo", "bar"));
    try {
      assertThat(sdkMeter.collectAll(testClock.now())).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    DoubleUpDownCounterSdk doubleUpDownCounter =
        sdkMeter
            .doubleUpDownCounterBuilder("testUpDownCounter")
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    doubleUpDownCounter.add(12d, Labels.empty());
    doubleUpDownCounter.add(12d);
    // TODO: This is not perfect because we compare double values using direct equal, maybe worth
    //  changing to do a proper comparison for double values, here and everywhere else.
    assertThat(sdkMeter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testUpDownCounter",
                "description",
                "ms",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.DoublePoint.create(
                            testClock.now() - SECOND_NANOS,
                            testClock.now(),
                            Labels.empty(),
                            24)))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleUpDownCounterSdk doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testUpDownCounter").build();
    BoundDoubleUpDownCounter bound = doubleUpDownCounter.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleUpDownCounter.add(12.1d, Labels.empty());
      bound.add(123.3d);
      doubleUpDownCounter.add(21.4d, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(321.5d);
      doubleUpDownCounter.add(111.1d, Labels.of("K", "V"));
      assertThat(sdkMeter.collectAll(testClock.now()))
          .containsExactly(
              MetricData.createDoubleSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testUpDownCounter",
                  "",
                  "1",
                  MetricData.DoubleSumData.create(
                      /* isMonotonic= */ false,
                      MetricData.AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          MetricData.DoublePoint.create(
                              startTime, testClock.now(), Labels.of("K", "V"), 555.9d),
                          MetricData.DoublePoint.create(
                              startTime, testClock.now(), Labels.empty(), 33.5d)))));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(222d);
      doubleUpDownCounter.add(11d, Labels.empty());

      assertThat(sdkMeter.collectAll(testClock.now()))
          .containsExactly(
              MetricData.createDoubleSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testUpDownCounter",
                  "",
                  "1",
                  MetricData.DoubleSumData.create(
                      /* isMonotonic= */ false,
                      MetricData.AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          MetricData.DoublePoint.create(
                              startTime, testClock.now(), Labels.of("K", "V"), 777.9d),
                          MetricData.DoublePoint.create(
                              startTime, testClock.now(), Labels.empty(), 44.5d)))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void stressTest() {
    final DoubleUpDownCounterSdk doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testUpDownCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleUpDownCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(doubleUpDownCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(doubleUpDownCounter.bind(Labels.of("K", "V")))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testUpDownCounter",
                "",
                "1",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        MetricData.DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.of("K", "V"), 80_000)))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleUpDownCounterSdk doubleUpDownCounter =
        sdkMeter.doubleUpDownCounterBuilder("testUpDownCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleUpDownCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(doubleUpDownCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(
                  doubleUpDownCounter.bind(Labels.of(keys[i], values[i])))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testUpDownCounter",
                "",
                "1",
                MetricData.DoubleSumData.create(
                    /* isMonotonic= */ false,
                    MetricData.AggregationTemporality.CUMULATIVE,
                    Arrays.asList(
                        MetricData.DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[0], values[0]),
                            40_000),
                        MetricData.DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[1], values[1]),
                            40_000),
                        MetricData.DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[2], values[2]),
                            40_000),
                        MetricData.DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[3], values[3]),
                            40_000)))));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundDoubleUpDownCounter boundDoubleUpDownCounter;

    private OperationUpdaterWithBinding(BoundDoubleUpDownCounter boundDoubleUpDownCounter) {
      this.boundDoubleUpDownCounter = boundDoubleUpDownCounter;
    }

    @Override
    void update() {
      boundDoubleUpDownCounter.add(9.0);
    }

    @Override
    void cleanup() {
      boundDoubleUpDownCounter.unbind();
    }
  }

  private static class OperationUpdaterDirectCall extends OperationUpdater {

    private final DoubleUpDownCounter doubleUpDownCounter;
    private final String key;
    private final String value;

    private OperationUpdaterDirectCall(
        DoubleUpDownCounter doubleUpDownCounter, String key, String value) {
      this.doubleUpDownCounter = doubleUpDownCounter;
      this.key = key;
      this.value = value;
    }

    @Override
    void update() {
      doubleUpDownCounter.add(11.0, Labels.of(key, value));
    }

    @Override
    void cleanup() {}
  }
}
