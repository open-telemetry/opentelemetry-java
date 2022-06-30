/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("PreferJavaTimeOverload")
class TimerTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  @Test
  void testTimer() {
    Timer timer =
        Timer.builder("testTimer")
            .description("This is a test timer")
            .tags("tag", "value")
            .register(Metrics.globalRegistry);

    timer.record(42, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimer")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(42_000)
                                        .hasCount(1)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.max")
                    .hasDescription("This is a test timer")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(42_000)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    Metrics.globalRegistry.remove(timer);
    timer.record(12, TimeUnit.SECONDS);

    // Histogram is synchronous and returns previous value after removal, max is asynchronous and is
    // removed completely.
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimer")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(42_000)
                                        .hasCount(1)
                                        .hasAttributes(attributeEntry("tag", "value")))));
  }

  @Test
  void testNanoPrecision() {
    Timer timer = Timer.builder("testNanoTimer").register(Metrics.globalRegistry);

    timer.record(1_234_000, TimeUnit.NANOSECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testNanoTimer")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(1.234)
                                        .hasCount(1)
                                        .hasAttributes(Attributes.empty()))),
            metric ->
                assertThat(metric)
                    .hasName("testNanoTimer.max")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point -> point.hasValue(1.234).hasAttributes(Attributes.empty()))));
  }

  @Test
  void testMicrometerHistogram() {
    // given
    Timer timer =
        Timer.builder("testTimer")
            .description("This is a test timer")
            .tags("tag", "value")
            .serviceLevelObjectives(
                Duration.ofSeconds(1),
                Duration.ofSeconds(10),
                Duration.ofSeconds(100),
                Duration.ofSeconds(1000))
            .distributionStatisticBufferLength(10)
            .register(Metrics.globalRegistry);

    timer.record(500, TimeUnit.MILLISECONDS);
    timer.record(5, TimeUnit.SECONDS);
    timer.record(50, TimeUnit.SECONDS);
    timer.record(500, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimer")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(555500)
                                        .hasCount(4)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.max")
                    .hasDescription("This is a test timer")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(500000)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.histogram")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(1)
                                        .hasAttributes(
                                            attributeEntry("le", "1000"),
                                            attributeEntry("tag", "value")),
                                point ->
                                    point
                                        .hasValue(2)
                                        .hasAttributes(
                                            attributeEntry("le", "10000"),
                                            attributeEntry("tag", "value")),
                                point ->
                                    point
                                        .hasValue(3)
                                        .hasAttributes(
                                            attributeEntry("le", "100000"),
                                            attributeEntry("tag", "value")),
                                point ->
                                    point
                                        .hasValue(4)
                                        .hasAttributes(
                                            attributeEntry("le", "1000000"),
                                            attributeEntry("tag", "value")))));
  }

  @Test
  void testMicrometerPercentiles() {
    // given
    Timer timer =
        Timer.builder("testTimer")
            .description("This is a test timer")
            .tags("tag", "value")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(Metrics.globalRegistry);

    timer.record(50, TimeUnit.MILLISECONDS);
    timer.record(100, TimeUnit.MILLISECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimer")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(150)
                                        .hasCount(2)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.max")
                    .hasDescription("This is a test timer")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(100)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.percentile")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("phi", "0.5"),
                                        attributeEntry("tag", "value")),
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("phi", "0.95"),
                                        attributeEntry("tag", "value")),
                                point ->
                                    point.hasAttributes(
                                        attributeEntry("phi", "0.99"),
                                        attributeEntry("tag", "value")))));
  }
}
