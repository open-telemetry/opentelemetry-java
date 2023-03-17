/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.github.netmikey.logunit.api.LogCapturer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.DoubleCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link SdkDoubleCounter}. */
class SdkDoubleCounterTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkDoubleCounterTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .registerMetricReader(sdkMeterReader)
          .setResource(RESOURCE)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(SdkDoubleCounter.class);

  @Test
  void add_PreventNullAttributes() {
    assertThatThrownBy(
            () -> sdkMeter.counterBuilder("testCounter").ofDoubles().build().add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter.counterBuilder("testCounter").ofDoubles().build();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
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
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testCounter")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
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
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();
    doubleCounter.add(12.1d, Attributes.empty());
    doubleCounter.add(123.3d, Attributes.builder().put("K", "V").build());
    doubleCounter.add(21.4d, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleCounter.add(321.5d, Attributes.builder().put("K", "V").build());
    doubleCounter.add(111.1d, Attributes.builder().put("K", "V").build());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testCounter")
                    .hasDescription("")
                    .hasUnit("")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(33.5),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(555.9)
                                            .hasAttributes(attributeEntry("K", "V")))));

    // Repeat to prove we keep previous values.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleCounter.add(222d, Attributes.builder().put("K", "V").build());
    doubleCounter.add(11d, Attributes.empty());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .isCumulative()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasAttributes(Attributes.empty())
                                            .hasValue(44.5),
                                    point ->
                                        point
                                            .hasStartEpochNanos(startTime)
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(777.9)
                                            .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  @SuppressLogger(SdkDoubleCounter.class)
  void doubleCounterAdd_Monotonicity() {
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();
    doubleCounter.add(-45.77d);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Counters can only increase. Instrument testCounter has recorded a negative value.");
  }

  @Test
  void stressTest() {
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000, 1, () -> doubleCounter.add(10, Attributes.builder().put("K", "V").build())));
    }

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(40000)
                                            .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    DoubleCounter doubleCounter = sdkMeter.counterBuilder("testCounter").ofDoubles().build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    IntStream.range(0, 4)
        .forEach(
            i ->
                stressTestBuilder.addOperation(
                    StressTestRunner.Operation.create(
                        2_000,
                        1,
                        () ->
                            doubleCounter.add(
                                10, Attributes.builder().put(keys[i], values[i]).build()))));

    stressTestBuilder.build().run();
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isCumulative()
                                .isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(20_000)
                                            .hasAttributes(attributeEntry(keys[0], values[0])),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(20_000)
                                            .hasAttributes(attributeEntry(keys[1], values[1])),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(20_000)
                                            .hasAttributes(attributeEntry(keys[2], values[2])),
                                    point ->
                                        point
                                            .hasStartEpochNanos(testClock.now())
                                            .hasEpochNanos(testClock.now())
                                            .hasValue(20_000)
                                            .hasAttributes(attributeEntry(keys[3], values[3])))));
  }
}
