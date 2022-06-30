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
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

@SuppressWarnings("PreferJavaTimeOverload")
class TimerSecondsTest {

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
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("s")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(23.345)
                                        .hasCount(3)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testTimerSeconds.max")
                    .hasDescription("This is a test timer")
                    .hasUnit("s")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(12.345)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    Metrics.globalRegistry.remove(timer);
    timer.record(12, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testTimerSeconds")
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test timer")
                    .hasUnit("s")
                    .hasHistogramSatisfying(
                        histogram ->
                            histogram.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasSum(23.345)
                                        .hasCount(3)
                                        .hasAttributes(attributeEntry("tag", "value")))));
  }
}
