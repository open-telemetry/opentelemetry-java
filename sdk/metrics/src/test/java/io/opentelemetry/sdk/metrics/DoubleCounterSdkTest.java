/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundDoubleCounter;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.StressTestRunner.OperationUpdater;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
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
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void add_PreventNullLabels() {
    assertThatThrownBy(
            () -> sdkMeter.counterBuilder("testCounter").ofDoubles().build().add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void bound_PreventNullLabels() {
    assertThatThrownBy(() -> sdkMeter.counterBuilder("testCounter").ofDoubles().build().bind(null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("labels");
  }

  @Test
  void collectMetrics_NoRecords() {
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();
    BoundDoubleCounter bound = doubleCounter.bind(Attributes.builder().put("foo", "bar").build());
    try {
      assertThat(sdkMeterProvider.collectAllMetrics()).isEmpty();
    } finally {
      bound.unbind();
    }
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithEmptyLabel() {
    DoubleCounter doubleCounter =
        sdkMeter
            .counterBuilder("testCounter")
            .ofDoubles()
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleCounter.add(12d, Attributes.empty());
    doubleCounter.add(12d);
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testCounter")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isMonotonic()
                    .isCumulative()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                .hasEpochNanos(testClock.now())
                                .hasAttributes(Attributes.empty())
                                .hasValue(24)));
  }

  @Test
  @SuppressWarnings("unchecked")
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();
    BoundDoubleCounter bound = doubleCounter.bind(Attributes.builder().put("K", "V").build());
    try {
      // Do some records using bounds and direct calls and bindings.
      doubleCounter.add(12.1d, Attributes.empty());
      bound.add(123.3d);
      doubleCounter.add(21.4d, Attributes.empty());
      // Advancing time here should not matter.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.add(321.5d);
      doubleCounter.add(111.1d, Attributes.builder().put("K", "V").build());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasResource(RESOURCE)
                      .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                      .hasName("testCounter")
                      .hasDescription("")
                      .hasUnit("1")
                      .hasDoubleSum()
                      .isMonotonic()
                      .isCumulative()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point).hasAttributes(Attributes.empty()).hasValue(33.5),
                          point ->
                              assertThat(point)
                                  .hasValue(555.9)
                                  .attributes()
                                  .hasSize(1)
                                  .containsEntry("K", "V")));

      // Repeat to prove we keep previous values.
      testClock.advance(Duration.ofNanos(SECOND_NANOS));
      bound.add(222d);
      doubleCounter.add(11d, Attributes.empty());
      assertThat(sdkMeterProvider.collectAllMetrics())
          .satisfiesExactly(
              metric ->
                  assertThat(metric)
                      .hasDoubleSum()
                      .isCumulative()
                      .points()
                      .allSatisfy(
                          point ->
                              assertThat(point)
                                  .hasStartEpochNanos(startTime)
                                  .hasEpochNanos(testClock.now()))
                      .satisfiesExactlyInAnyOrder(
                          point ->
                              assertThat(point).hasAttributes(Attributes.empty()).hasValue(44.5),
                          point ->
                              assertThat(point)
                                  .hasAttributes(Attributes.of(stringKey("K"), "V"))
                                  .hasValue(777.9)));
    } finally {
      bound.unbind();
    }
  }

  @Test
  void doubleCounterAdd_Monotonicity() {
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();

    assertThatThrownBy(() -> doubleCounter.add(-45.77d, Attributes.empty()))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  void boundDoubleCounterAdd_Monotonicity() {
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();

    assertThatThrownBy(() -> doubleCounter.bind(Attributes.empty()).add(-9.3))
        .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  @SuppressWarnings("unchecked")
  void stressTest() {
    final DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((DoubleCounterSdk) doubleCounter)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 2, new OperationUpdaterDirectCall(doubleCounter, "K", "V")));
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              2,
              new OperationUpdaterWithBinding(
                  doubleCounter.bind(Attributes.builder().put("K", "V").build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasName("testCounter")
                    .hasDoubleSum()
                    .isCumulative()
                    .isMonotonic()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasValue(80_000)
                                .attributes()
                                .hasSize(1)
                                .containsEntry("K", "V")));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    final String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    final String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    final DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder()
            .setInstrument((DoubleCounterSdk) doubleCounter)
            .setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000, 1, new OperationUpdaterDirectCall(doubleCounter, keys[i], values[i])));

      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              new OperationUpdaterWithBinding(
                  doubleCounter.bind(Attributes.builder().put(keys[i], values[i]).build()))));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterProvider.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationLibrary(INSTRUMENTATION_LIBRARY_INFO)
                    .hasDoubleSum()
                    .isCumulative()
                    .isMonotonic()
                    .points()
                    .allSatisfy(
                        point ->
                            assertThat(point)
                                .hasStartEpochNanos(testClock.now())
                                .hasEpochNanos(testClock.now())
                                .hasValue(40_000))
                    .extracting(point -> point.getAttributes())
                    .containsExactlyInAnyOrder(
                        Attributes.of(stringKey(keys[0]), values[0]),
                        Attributes.of(stringKey(keys[1]), values[1]),
                        Attributes.of(stringKey(keys[2]), values[2]),
                        Attributes.of(stringKey(keys[3]), values[3])));
  }

  private static class OperationUpdaterWithBinding extends OperationUpdater {
    private final BoundDoubleCounter boundDoubleCounter;

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
      doubleCounter.add(11.0, Attributes.builder().put(key, value).build());
    }

    @Override
    void cleanup() {}
  }
}
