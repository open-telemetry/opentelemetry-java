/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.MockClock;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LongTaskTimerHistogramTest {

  static MockClock clock = new MockClock();

  @RegisterExtension
  static final MicrometerTestingExtension testing =
      new MicrometerTestingExtension() {
        @Override
        OpenTelemetryMeterRegistryBuilder configureOtelRegistry(
            OpenTelemetryMeterRegistryBuilder registry) {
          return registry.setClock(clock);
        }
      };

  @Test
  void testMicrometerHistogram() {
    LongTaskTimer timer =
        LongTaskTimer.builder("testLongTaskTimerHistogram")
            .description("This is a test timer")
            .serviceLevelObjectives(Duration.ofMillis(100), Duration.ofMillis(1000))
            .distributionStatisticBufferLength(10)
            .register(Metrics.globalRegistry);

    LongTaskTimer.Sample sample1 = timer.start();
    // only active tasks count
    timer.start().stop();
    clock.add(Duration.ofMillis(100));
    LongTaskTimer.Sample sample2 = timer.start();
    LongTaskTimer.Sample sample3 = timer.start();
    clock.add(Duration.ofMillis(10));

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.active")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test timer")
                    .hasUnit("tasks")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point -> point.hasValue(3).hasAttributes(Attributes.empty()))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.duration")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(Attributes.empty())
                                            .satisfies(
                                                pointData ->
                                                    assertThat(pointData.getValue())
                                                        .isPositive()))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.histogram")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasAttributes(attributeEntry("le", "100")).hasValue(2),
                                point ->
                                    point
                                        .hasAttributes(attributeEntry("le", "1000"))
                                        .hasValue(3))));

    sample1.stop();
    sample2.stop();
    sample3.stop();

    // Continues to report 0 after stopped.
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.active")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test timer")
                    .hasUnit("tasks")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point -> point.hasValue(0).hasAttributes(Attributes.empty()))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.duration")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point -> point.hasValue(0).hasAttributes(Attributes.empty()))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.histogram")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point.hasValue(0).hasAttributes(attributeEntry("le", "100")),
                                point ->
                                    point
                                        .hasValue(0)
                                        .hasAttributes(attributeEntry("le", "1000")))));
  }
}
