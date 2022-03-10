/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometershim;

import static io.opentelemetry.micrometershim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Timer;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("PreferJavaTimeOverload")
class TimerSecondsTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing =
      new MicrometerTestingExtension() {

        @Override
        OpenTelemetryMeterRegistryBuilder configureRegistry(
            OpenTelemetryMeterRegistryBuilder registry) {
          return registry.setBaseTimeUnit(TimeUnit.SECONDS);
        }
      };

  @Test
  void testTimerWithBaseUnitSeconds() {
    Timer timer =
        Timer.builder("testTimerSeconds")
            .description("This is a test timer")
            .tags("tag", "value")
            .register(Metrics.globalRegistry);

    timer.record(1, TimeUnit.SECONDS);
    timer.record(10, TimeUnit.SECONDS);
    timer.record(12_345, TimeUnit.MILLISECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimerSeconds")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("s")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(23.345)
                                .hasCount(3)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testTimerSeconds.max")
                    .hasDescription("This is a test timer")
                    .hasUnit("s")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(12.345)
                                .attributes()
                                .containsEntry("tag", "value")));

    Metrics.globalRegistry.remove(timer);
    timer.record(12, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimerSeconds")
                    .hasDoubleHistogram()
                    .points()
                    .noneSatisfy(point -> assertThat(point).hasSum(35.345).hasCount(4)));
  }
}
