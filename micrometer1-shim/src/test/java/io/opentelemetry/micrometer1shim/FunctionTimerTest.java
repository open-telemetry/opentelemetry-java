/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.FunctionTimer;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class FunctionTimerTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  final TestTimer timerObj = new TestTimer();
  final TestTimer anotherTimerObj = new TestTimer();

  @BeforeEach
  void cleanupTimers() {
    timerObj.reset();
    anotherTimerObj.reset();
  }

  @Test
  void testFunctionTimer() throws InterruptedException {
    FunctionTimer functionTimer =
        FunctionTimer.builder(
                "testFunctionTimer",
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
                    .hasName("testFunctionTimer.count")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test function timer")
                    .hasUnit("1")
                    .hasLongSum()
                    .isMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(1)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testFunctionTimer.sum")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test function timer")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(42_000)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))));

    Metrics.globalRegistry.remove(functionTimer);
    assertThat(testing.collectAllMetrics()).isEmpty();
  }

  @Test
  void testNanoPrecision() {
    FunctionTimer.builder(
            "testNanoFunctionTimer",
            timerObj,
            TestTimer::getCount,
            TestTimer::getTotalTimeNanos,
            TimeUnit.NANOSECONDS)
        .register(Metrics.globalRegistry);

    timerObj.add(1_234_000, TimeUnit.NANOSECONDS);

    assertThat(testing.collectAllMetrics())
        .anySatisfy(
            metric ->
                assertThat(metric)
                    .hasName("testNanoFunctionTimer.sum")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .points()
                    .satisfiesExactly(point -> assertThat(point).hasValue(1.234).attributes()));
  }

  @Test
  void functionTimersWithSameNameAndDifferentTags() {
    FunctionTimer.builder(
            "testFunctionTimerWithTags",
            timerObj,
            TestTimer::getCount,
            TestTimer::getTotalTimeNanos,
            TimeUnit.NANOSECONDS)
        .tags("tag", "1")
        .register(Metrics.globalRegistry);

    FunctionTimer.builder(
            "testFunctionTimerWithTags",
            anotherTimerObj,
            TestTimer::getCount,
            TestTimer::getTotalTimeNanos,
            TimeUnit.NANOSECONDS)
        .tags("tag", "2")
        .register(Metrics.globalRegistry);

    timerObj.add(12, TimeUnit.SECONDS);
    anotherTimerObj.add(42, TimeUnit.SECONDS);

    assertThat(testing.collectAllMetrics())
        .anySatisfy(
            metric ->
                assertThat(metric)
                    .hasName("testFunctionTimerWithTags.sum")
                    .hasUnit("ms")
                    .hasDoubleSum()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(12_000)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "1")))
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(42_000)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "2"))));
  }
}
