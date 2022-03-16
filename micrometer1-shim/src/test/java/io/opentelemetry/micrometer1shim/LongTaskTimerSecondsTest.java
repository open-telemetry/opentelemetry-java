/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class LongTaskTimerSecondsTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing =
      new MicrometerTestingExtension() {

        @Override
        OpenTelemetryMeterRegistryBuilder configureOtelRegistry(
            OpenTelemetryMeterRegistryBuilder registry) {
          return registry.setBaseTimeUnit(TimeUnit.SECONDS);
        }
      };

  @Test
  void testLongTaskTimerWithBaseUnitSeconds() throws InterruptedException {
    // given
    LongTaskTimer timer =
        LongTaskTimer.builder("testLongTaskTimerSeconds")
            .description("This is a test long task timer")
            .tags("tag", "value")
            .register(Metrics.globalRegistry);

    // when
    LongTaskTimer.Sample sample = timer.start();

    // then
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerSeconds.active")
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
                    .hasName("testLongTaskTimerSeconds.duration")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test long task timer")
                    .hasUnit("s")
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
                    .hasName("testLongTaskTimerSeconds.active")
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
                    .hasName("testLongTaskTimerSeconds.duration")
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
