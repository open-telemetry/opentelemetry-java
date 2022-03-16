/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class FunctionCounterTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  final AtomicLong num = new AtomicLong(12);
  final AtomicLong anotherNum = new AtomicLong(13);

  @Test
  void testFunctionCounter() {
    FunctionCounter counter =
        FunctionCounter.builder("testFunctionCounter", num, AtomicLong::get)
            .description("This is a test function counter")
            .tags("tag", "value")
            .baseUnit("items")
            .register(Metrics.globalRegistry);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testFunctionCounter")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test function counter")
                    .hasUnit("items")
                    .hasDoubleSum()
                    .isMonotonic()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasValue(12)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))));

    Metrics.globalRegistry.remove(counter);
    assertThat(testing.collectAllMetrics()).isEmpty();
  }

  @Test
  // TODO(anuraaga): Enable after https://github.com/open-telemetry/opentelemetry-java/pull/4222
  @Disabled
  void functionCountersWithSameNameAndDifferentTags() {
    FunctionCounter.builder("testFunctionCounterWithTags", num, AtomicLong::get)
        .description("First description wins")
        .tags("tag", "1")
        .baseUnit("items")
        .register(Metrics.globalRegistry);
    FunctionCounter.builder("testFunctionCounterWithTags", anotherNum, AtomicLong::get)
        .description("ignored")
        .tags("tag", "2")
        .baseUnit("items")
        .register(Metrics.globalRegistry);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testFunctionCounterWithTags")
                    .hasDescription("First description wins")
                    .hasUnit("items")
                    .hasDoubleSum()
                    .isMonotonic()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(12)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "1")))
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(13)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "2"))));
  }
}
