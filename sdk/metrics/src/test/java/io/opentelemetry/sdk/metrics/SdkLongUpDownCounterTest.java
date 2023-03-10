/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.LongUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkLongUpDownCounter}. */
class SdkLongUpDownCounterTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkLongUpDownCounterTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(sdkMeterReader)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void add_PreventNullAttributes() {
    assertThatThrownBy(() -> sdkMeter.upDownCounterBuilder("testCounter").build().add(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter.upDownCounterBuilder("testUpDownCounter").build();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    LongUpDownCounter longUpDownCounter =
        sdkMeter
            .upDownCounterBuilder("testUpDownCounter")
            .setDescription("description")
            .setUnit("By")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longUpDownCounter.add(12, Attributes.empty());
    longUpDownCounter.add(12);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasDescription("description")
                    .hasUnit("By")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(24))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongUpDownCounter longUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").build();
    longUpDownCounter.add(12, Attributes.empty());
    longUpDownCounter.add(123, Attributes.builder().put("K", "V").build());
    longUpDownCounter.add(21, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longUpDownCounter.add(321, Attributes.builder().put("K", "V").build());
    longUpDownCounter.add(111, Attributes.builder().put("K", "V").build());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(33),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(attributeEntry("K", "V"))
                                            .hasValue(555))));

    // Repeat to prove we keep previous values.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longUpDownCounter.add(222, Attributes.builder().put("K", "V").build());
    longUpDownCounter.add(11, Attributes.empty());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(44),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(attributeEntry("K", "V"))
                                            .hasValue(777))));
  }

  @Test
  void stressTest() {
    LongUpDownCounter longUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              2_000,
              1,
              () -> longUpDownCounter.add(10, Attributes.builder().put("K", "V").build())));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(80_000)
                                            .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    LongUpDownCounter longUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    IntStream.range(0, 4)
        .forEach(
            i ->
                stressTestBuilder.addOperation(
                    StressTestRunner.Operation.create(
                        1_000,
                        2,
                        () ->
                            longUpDownCounter.add(
                                10, Attributes.builder().put(keys[i], values[i]).build()))));

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(10_000)
                                            .hasAttributes(attributeEntry(keys[0], values[0])),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(10_000)
                                            .hasAttributes(attributeEntry(keys[1], values[1])),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(10_000)
                                            .hasAttributes(attributeEntry(keys[2], values[2])),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(10_000)
                                            .hasAttributes(attributeEntry(keys[3], values[3])))));
  }
}
