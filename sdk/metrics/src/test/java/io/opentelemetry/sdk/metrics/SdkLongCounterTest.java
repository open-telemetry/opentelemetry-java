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
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.testing.exporter.InMemoryMetricReader;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

/** Unit tests for {@link SdkLongCounter}. */
class SdkLongCounterTest {
  private static final long SECOND_NANOS = 1_000_000_000;
  private static final Resource RESOURCE =
      Resource.create(Attributes.of(stringKey("resource_key"), "resource_value"));
  private static final InstrumentationScopeInfo INSTRUMENTATION_SCOPE_INFO =
      InstrumentationScopeInfo.create(SdkLongCounterTest.class.getName());
  private final TestClock testClock = TestClock.create();
  private final InMemoryMetricReader sdkMeterReader = InMemoryMetricReader.create();
  private final SdkMeterProvider sdkMeterProvider =
      SdkMeterProvider.builder()
          .setClock(testClock)
          .setResource(RESOURCE)
          .registerMetricReader(sdkMeterReader)
          .build();
  private final Meter sdkMeter = sdkMeterProvider.get(getClass().getName());

  @RegisterExtension LogCapturer logs = LogCapturer.create().captureForType(SdkLongCounter.class);

  @Test
  void add_PreventNullAttributes() {
    assertThatThrownBy(() -> sdkMeter.counterBuilder("testCounter").build().add(1, null))
        .isInstanceOf(NullPointerException.class)
        .hasMessage("attributes");
  }

  @Test
  void collectMetrics_NoRecords() {
    sdkMeter.counterBuilder("Counter").build();
    assertThat(sdkMeterReader.collectAllMetrics()).isEmpty();
  }

  @Test
  void collectMetrics_WithEmptyAttributes() {
    LongCounter longCounter =
        sdkMeter.counterBuilder("testCounter").setDescription("description").setUnit("By").build();
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longCounter.add(12, Attributes.empty());
    longCounter.add(12);
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testCounter")
                    .hasDescription("description")
                    .hasUnit("By")
                    .hasLongSumSatisfying(
                        longSum ->
                            longSum
                                .isMonotonic()
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
    LongCounter longCounter = sdkMeter.counterBuilder("testCounter").build();
    longCounter.add(12, Attributes.empty());
    longCounter.add(123, Attributes.builder().put("K", "V").build());
    longCounter.add(21, Attributes.empty());
    // Advancing time here should not matter.
    testClock.advance(Duration.ofNanos(SECOND_NANOS));
    longCounter.add(321, Attributes.builder().put("K", "V").build());
    longCounter.add(111, Attributes.builder().put("K", "V").build());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testCounter")
                    .hasLongSumSatisfying(
                        longSum ->
                            longSum
                                .isMonotonic()
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
    longCounter.add(222, Attributes.builder().put("K", "V").build());
    longCounter.add(11, Attributes.empty());
    assertThat(sdkMeterReader.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasResource(RESOURCE)
                    .hasInstrumentationScope(INSTRUMENTATION_SCOPE_INFO)
                    .hasName("testCounter")
                    .hasLongSumSatisfying(
                        longSum ->
                            longSum
                                .isMonotonic()
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
  @SuppressLogger(SdkLongCounter.class)
  void longCounterAdd_Monotonicity() {
    LongCounter longCounter = sdkMeter.counterBuilder("testCounter").build();
    longCounter.add(-45);
    assertThat(sdkMeterReader.collectAllMetrics()).hasSize(0);
    logs.assertContains(
        "Counters can only increase. Instrument testCounter has recorded a negative value.");
  }
}
