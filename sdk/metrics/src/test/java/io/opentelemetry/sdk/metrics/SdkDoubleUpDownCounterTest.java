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
import io.opentelemetry.api.metrics.DoubleUpDownCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SdkDoubleUpDownCounter}. */
class SdkDoubleUpDownCounterTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkDoubleUpDownCounterTest.class.getName());
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
    assertThatThrownBy(
            () ->
                sdkMeter
                    .upDownCounterBuilder("testUpDownCounter")
                    .ofDoubles()
                    .build()
                    .add(1.0, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter
            .upDownCounterBuilder("testUpDownCounter")
            .ofDoubles()
            .setDescription("description")
            .setUnit("ms")
            .build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleUpDownCounter.add(12d, Attributes.empty());
    doubleUpDownCounter.add(12d);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasDescription("description")
                    .hasUnit("ms")
                    .hasDoubleSumSatisfying(
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
    DoubleUpDownCounter doubleUpDownCounter =
        sdkMeter.upDownCounterBuilder("testUpDownCounter").ofDoubles().build();
    doubleUpDownCounter.add(12.1d, Attributes.empty());
    doubleUpDownCounter.add(123.3d, Attributes.builder().put("K", "V").build());
    doubleUpDownCounter.add(21.4d, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    doubleUpDownCounter.add(321.5d, Attributes.builder().put("K", "V").build());
    doubleUpDownCounter.add(111.1d, Attributes.builder().put("K", "V").build());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
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
    doubleUpDownCounter.add(222d, Attributes.builder().put("K", "V").build());
    doubleUpDownCounter.add(11d, Attributes.empty());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testUpDownCounter")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
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
                                            .hasAttributes(attributeEntry("K", "V"))
                                            .hasValue(777.9))));
  }

  @Test
  void testToString() {
    String expected =
        "SdkDoubleUpDownCounter{descriptor=InstrumentDescriptor{name=testUpDownCounter, description=description, unit=ms, type=UP_DOWN_COUNTER, valueType=DOUBLE, advice=Advice{explicitBucketBoundaries=null, attributes=null}}}";
    DoubleUpDownCounter counter =
        sdkMeter
            .upDownCounterBuilder("testUpDownCounter")
            .ofDoubles()
            .setDescription("description")
            .setUnit("ms")
            .build();
    assertThat(counter).hasToString(expected);
  }
}
