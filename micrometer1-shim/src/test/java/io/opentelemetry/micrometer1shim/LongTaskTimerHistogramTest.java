/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;

import io.micrometer.core.instrument.LongTaskTimer;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.MockClock;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.time.Duration;
import org.assertj.core.api.Assertions;
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
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("tasks")
                    .hasLongSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point -> assertThat(point).hasValue(3).attributes().isEmpty()),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.duration")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point -> {
                          assertThat(point).attributes().isEmpty();
                          // any value >0 - duration of currently running tasks
                          Assertions.assertThat(point.getValue()).isPositive();
                        }),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.histogram")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point).hasValue(2).attributes().containsEntry("le", "100"))
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(3)
                                .attributes()
                                .containsEntry("le", "1000")));

    sample1.stop();
    sample2.stop();
    sample3.stop();

    // Continues to report 0 after stopped.
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.active")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("tasks")
                    .hasLongSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point -> assertThat(point).hasValue(0).attributes().isEmpty()),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.duration")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .isNotMonotonic()
                    .points()
                    .satisfiesExactly(
                        point -> {
                          assertThat(point).attributes().isEmpty();
                          // any value >0 - duration of currently running tasks
                          Assertions.assertThat(point.getValue()).isZero();
                        }),
            metric ->
                assertThat(metric)
                    .hasName("testLongTaskTimerHistogram.histogram")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point).hasValue(0).attributes().containsEntry("le", "100"))
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(0)
                                .attributes()
                                .containsEntry("le", "1000")));
  }
}
