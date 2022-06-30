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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LongTaskTimerTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  @Test
  void testLongTaskTimer() throws InterruptedException {
    LongTaskTimer timer =
        LongTaskTimer.builder("testLongTaskTimer")
            .description("This is a test long task timer")
            .tags("tag", "value")
            .register(Metrics.globalRegistry);

    LongTaskTimer.Sample sample = timer.start();

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.active")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test long task timer")
                    .hasUnit("tasks")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(1)
                                            .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.duration")
                    .hasDescription("This is a test long task timer")
                    .hasUnit("ms")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasAttributes(attributeEntry("tag", "value"))
                                            .satisfies(
                                                pointData ->
                                                    assertThat(pointData.getValue())
                                                        .isPositive()))));

    // when
    TimeUnit.MILLISECONDS.sleep(100);
    sample.stop();

    // then
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.active")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(0)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.duration")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(0)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    // when timer is removed from the registry
    Metrics.globalRegistry.remove(timer);
    timer.start();

    // then no tasks are active after starting a new sample
    assertThat(testing.collectAllMetrics()).isEmpty();
  }
}
