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
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounter.BoundDoubleCounter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.internal.TestClock;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoublePoint;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DoubleCounterSdk}. */
class DoubleCounterSdkTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationLibraryInfo INSTRUMENTATION_LIBRARY_INFO =
      InstrumentationLibraryInfo.create(DoubleCounterSdkTest.class.getName(), null);
  private final TestClock testClock = TestClock.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder().setClock(testClock).setResource(RESOURCE).build();
  private final SdkMeter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.doubleCounterBuilder("testCounter").build().add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.doubleCounterBuilder("testCounter").build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter bound = doubleCounter.bind(Labels.of("foo", "bar"));
    try {
      assertThat(sdkMeter.collectAll(testClock.now())).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  void collectMetrics_WithEmptyLabel() {
    DoubleCounterSdk doubleCounter =
        sdkMeter
            .doubleCounterBuilder("testCounter")
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advanceNanos(SECOND_NANOS);
    doubleCounter.add(12d, Labels.empty());
    doubleCounter.add(12d);
    // TODO: This is not perfect because we compare double values using direct equal, maybe worth
    //  changing to do a proper comparison for double values, here and everywhere else.
    assertThat(sdkMeter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testCounter",
                "description",
                "ms",
                DoubleSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        DoublePoint.create(
                            testClock.now() - SECOND_NANOS,
                            testClock.now(),
                            Labels.empty(),
                            24)))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testCounter").build();
    BoundDoubleCounter bound = doubleCounter.bind(Labels.of("K", "V"));
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleCounter.add(12.1d, Labels.empty());
      bound.add(123.3d);
      doubleCounter.add(21.4d, Labels.empty());
      // Advancing time here should not matter.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(321.5d);
      doubleCounter.add(111.1d, Labels.of("K", "V"));
      assertThat(sdkMeter.collectAll(testClock.now()))
          .containsExactly(
              MetricData.createDoubleSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testCounter",
                  "",
                  "1",
                  DoubleSumData.create(
                      /* isMonotonic= */ true,
                      AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          DoublePoint.create(
                              startTime, testClock.now(), Labels.of("K", "V"), 555.9d),
                          DoublePoint.create(startTime, testClock.now(), Labels.empty(), 33.5d)))));

      // Repeat to prove we keep previous values.
      testClock.advanceNanos(SECOND_NANOS);
      bound.add(222d);
      doubleCounter.add(11d, Labels.empty());
      assertThat(sdkMeter.collectAll(testClock.now()))
          .containsExactly(
              MetricData.createDoubleSum(
                  RESOURCE,
                  INSTRUMENTATION_LIBRARY_INFO,
                  "testCounter",
                  "",
                  "1",
                  DoubleSumData.create(
                      /* isMonotonic= */ true,
                      AggregationTemporality.CUMULATIVE,
                      Arrays.asList(
                          DoublePoint.create(
                              startTime, testClock.now(), Labels.of("K", "V"), 777.9d),
                          DoublePoint.create(startTime, testClock.now(), Labels.empty(), 44.5d)))));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void doubleCounterAdd_Monotonicity() {
    DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testCounter").build();

    assertThatThrownBy(() -> doubleCounter.add(-45.77d, Labels.empty()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void boundDoubleCounterAdd_Monotonicity() {
    DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testCounter").build();

    assertThatThrownBy(() -> doubleCounter.bind(Labels.empty()).add(-9.3))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void stressTest() {
    final DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(doubleCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterWithBinding(doubleCounter.bind(Labels.of("K", "V")))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testCounter",
                "",
                "1",
                DoubleSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Collections.singletonList(
                        DoublePoint.create(
                            testClock.now(), testClock.now(), Labels.of("K", "V"), 80_000)))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleCounterSdk doubleCounter = sdkMeter.doubleCounterBuilder("testCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setInstrument(doubleCounter).setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(doubleCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(doubleCounter.bind(Labels.of(keys[i], values[i])))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeter.collectAll(testClock.now()))
        .containsExactly(
            MetricData.createDoubleSum(
                RESOURCE,
                INSTRUMENTATION_LIBRARY_INFO,
                "testCounter",
                "",
                "1",
                DoubleSumData.create(
                    /* isMonotonic= */ true,
                    AggregationTemporality.CUMULATIVE,
                    Arrays.asList(
                        DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[0], values[0]),
                            40_000),
                        DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[1], values[1]),
                            40_000),
                        DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[2], values[2]),
                            40_000),
                        DoublePoint.create(
                            testClock.now(),
                            testClock.now(),
                            Labels.of(keys[3], values[3]),
                            40_000)))));
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
      doubleCounter.add(11.0, Labels.of(key, value));
    }

    @Override
    void cleanup() {}
  }
}
