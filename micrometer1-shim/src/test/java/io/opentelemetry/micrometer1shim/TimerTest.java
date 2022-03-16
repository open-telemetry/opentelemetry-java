/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
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
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(42_000)
                                .hasCount(1)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.max")
                    .hasDescription("This is a test timer")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(42_000)
                                .attributes()
                                .containsEntry("tag", "value")));

    Metrics.globalRegistry.remove(timer);
    timer.record(12, TimeUnit.SECONDS);

    // Histogram is synchronous and returns previous value after removal, max is asynchronous and is
    // removed completely.
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimer")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(42_000)
                                .hasCount(1)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))));
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
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point).hasSum(1.234).hasCount(1).attributes().isEmpty()),
            metric ->
                assertThat(metric)
                    .hasName("testNanoTimer.max")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(point -> assertThat(point).hasValue(1.234).attributes().isEmpty()));
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
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(555500)
                                .hasCount(4)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.max")
                    .hasDescription("This is a test timer")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(500000)
                                .attributes()
                                .containsEntry("tag", "value")),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.histogram")
                    .hasDoubleGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point).hasValue(1).attributes().containsEntry("le", "1000"),
                        point ->
                            assertThat(point).hasValue(2).attributes().containsEntry("le", "10000"),
                        point ->
                            assertThat(point)
                                .hasValue(3)
                                .attributes()
                                .containsEntry("le", "100000"),
                        point ->
                            assertThat(point)
                                .hasValue(4)
                                .attributes()
                                .containsEntry("le", "1000000")));
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
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(150)
                                .hasCount(2)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.max")
                    .hasDescription("This is a test timer")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(100)
                                .attributes()
                                .containsEntry("tag", "value")),
            metric ->
                assertThat(metric)
                    .hasName("testTimer.percentile")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(point -> assertThat(point).attributes().containsEntry("phi", "0.5"))
                    .anySatisfy(
                        point -> assertThat(point).attributes().containsEntry("phi", "0.95"))
                    .anySatisfy(
                        point -> assertThat(point).attributes().containsEntry("phi", "0.99")));
  }
}
