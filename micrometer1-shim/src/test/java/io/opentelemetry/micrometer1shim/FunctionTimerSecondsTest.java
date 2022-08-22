/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class FunctionTimerSecondsTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing =
      new MicrometerTestingExtension() {

        @Override
        OpenTelemetryMeterRegistryBuilder configureOtelRegistry(
            OpenTelemetryMeterRegistryBuilder registry) {
          return registry.setBaseTimeUnit(TimeUnit.SECONDS);
        }
      };

  final TestTimer timerObj = new TestTimer();

  @BeforeEach
  void cleanupTimer() {
    timerObj.reset();
  }

  @Test
  void testFunctionTimerWithBaseUnitSeconds() {
    FunctionTimer functionTimer =
        FunctionTimer.builder(
                "testFunctionTimerSeconds",
                timerObj,
                TestTimer::getCount,
                TestTimer::getTotalTimeNanos,
                TimeUnit.NANOSECONDS)
            .description("This is a test function timer")
            .tags("tag", "value")
            .register(Metrics.globalRegistry);

    timerObj.add(42, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testFunctionTimerSeconds.count")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test function timer")
                    .hasUnit("1")
                    .hasLongSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(1)
                                            .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testFunctionTimerSeconds.sum")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test function timer")
                    .hasUnit("s")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(42)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    Metrics.globalRegistry.remove(functionTimer);
    assertThat(testing.collectAllMetrics()).isEmpty();
  }
}
