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
import io.opentelemetry.api.incubator.metrics.ExtendedLongGaugeBuilder;
import io.opentelemetry.api.incubator.metrics.LongGauge;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.api.metrics.ObservableLongGauge;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.stream.IntStream;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkLongGauge}. */
class SdkLongGaugeTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkLongGaugeTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader cumulativeReader = InMemoryMetricReader.create();
  private final InMemoryMetricReader deltaReader = InMemoryMetricReader.createDelta();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .registerMetricReader(cumulativeReader)
          .registerMetricReader(deltaReader)
          .setResource(RESOURCE)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @Test
  void set_PreventNullAttributes() {
    assertThatThrownBy(
            () ->
                ((ExtendedLongGaugeBuilder) sdkMeter.gaugeBuilder("testGauge").ofLongs())
                    .build()
                    .set(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void observable_RemoveCallback() {
    ObservableLongGauge gauge =
        sdkMeter
            .gaugeBuilder("testGauge")
            .ofLongs()
            .buildWithCallback(measurement -> measurement.record(10));

    Assertions.assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testGauge")
                    .hasLongGaugeSatisfying(
                        longGauge -> longGauge.hasPointsSatisfying(point -> {})));

    gauge.close();

    Assertions.assertThat(cumulativeReader.collectAllMetrics()).hasSize(0);
  }

  @Test
  void collectMetrics_NoRecords() {
    ((ExtendedLongGaugeBuilder) sdkMeter.gaugeBuilder("testGauge").ofLongs()).build();
    assertThat(cumulativeReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    LongGauge longGauge =
        ((ExtendedLongGaugeBuilder)
                sdkMeter
                    .gaugeBuilder("testGauge")
                    .ofLongs()
                    .setDescription("description")
                    .setUnit("K"))
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longGauge.set(12, Attributes.empty());
    longGauge.set(13);
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDescription("description")
                    .hasUnit("K")
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now() - SECOND_NANOS)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(13))));
  }

  @Test
  void collectMetrics_WithMultipleCollects() {
    long startTime = testClock.now();
    LongGauge longGauge =
        ((ExtendedLongGaugeBuilder) sdkMeter.gaugeBuilder("testGauge").ofLongs()).build();
    longGauge.set(12, Attributes.empty());
    longGauge.set(12, Attributes.builder().put("K", "V").build());
    longGauge.set(21, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longGauge.set(321, Attributes.builder().put("K", "V").build());
    longGauge.set(111, Attributes.builder().put("K", "V").build());
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDescription("")
                    .hasUnit("")
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(21),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(111)
                                        .hasAttributes(attributeEntry("K", "V")))));
    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasDescription("")
                    .hasUnit("")
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(21),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(111)
                                        .hasAttributes(attributeEntry("K", "V")))));

    // Repeat to prove we keep previous values.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longGauge.set(222, Attributes.builder().put("K", "V").build());
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasAttributes(Attributes.empty())
                                        .hasValue(21),
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(222)
                                        .hasAttributes(attributeEntry("K", "V")))));
    // Delta reader should only have point for {K=V} series, since the {} did not have any
    // measurements
    assertThat(deltaReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(startTime + SECOND_NANOS)
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(222)
                                        .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  void stressTest() {
    LongGauge longGauge =
        ((ExtendedLongGaugeBuilder) sdkMeter.gaugeBuilder("testGauge").ofLongs()).build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    for (int i = 0; i < 4; i++) {
      stressTestBuilder.addOperation(
          StressTestRunner.Operation.create(
              1_000,
              1,
              () -> {
                longGauge.set(10, Attributes.builder().put("K", "V").build());
                longGauge.set(11, Attributes.builder().put("K", "V").build());
              }));
    }

    stressTestBuilder.build().run();
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testGauge")
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry("K", "V")))));
  }

  @Test
  void stressTest_WithDifferentLabelSet() {
    String[] keys = {"Key_1", "Key_2", "Key_3", "Key_4"};
    String[] values = {"Value_1", "Value_2", "Value_3", "Value_4"};
    LongGauge longGauge =
        ((ExtendedLongGaugeBuilder) sdkMeter.gaugeBuilder("testGauge").ofLongs()).build();

    StressTestRunner.Builder stressTestBuilder =
        StressTestRunner.builder().setCollectionIntervalMs(100);

    IntStream.range(0, 4)
        .forEach(
            i ->
                stressTestBuilder.addOperation(
                    StressTestRunner.Operation.create(
                        2_000,
                        1,
                        () -> {
                          longGauge.set(10, Attributes.builder().put(keys[i], values[i]).build());
                          longGauge.set(11, Attributes.builder().put(keys[i], values[i]).build());
                        })));

    stressTestBuilder.build().run();
    assertThat(cumulativeReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasLongGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[0], values[0])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[1], values[1])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[2], values[2])),
                                point ->
                                    point
                                        .hasStartEpochNanos(testClock.now())
                                        .hasEpochNanos(testClock.now())
                                        .hasValue(11)
                                        .hasAttributes(attributeEntry(keys[3], values[3])))));
  }
}
