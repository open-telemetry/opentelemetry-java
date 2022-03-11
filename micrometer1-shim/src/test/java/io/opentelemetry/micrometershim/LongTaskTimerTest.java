/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometershim;

import static io.opentelemetry.micrometershim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
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
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test long task timer")
                    .hasUnit("tasks")
                    .hasLongSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.duration")
                    .hasDescription("This is a test long task timer")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point -> {
                          assertThat(point)
                              .attributes()
                              .containsOnly(attributeEntry("tag", "value"));
                          // any value >0 - duration of currently running tasks
                          assertThat(point.getValue()).isPositive();
                        }));

    // when
    TimeUnit.MILLISECONDS.sleep(100);
    sample.stop();

    // then
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.active")
                    .hasLongSum()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(0)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimer.duration")
                    .hasDoubleSum()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(0)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))));

    // when timer is removed from the registry
    Metrics.globalRegistry.remove(timer);
    timer.start();

    // then no tasks are active after starting a new sample
    assertThat(testing.collectAllMetrics()).isEmpty();
  }
}
