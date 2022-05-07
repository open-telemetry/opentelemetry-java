/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.FunctionCounter;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.internal.testing.slf4j.SuppressLogger;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.internal.state.MetricStorageRegistry;
import java.util.concurrent.atomic.AtomicLong;
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
                    .hasInstrumentationScope(
                        InstrumentationScopeInfo.create(INSTRUMENTATION_NAME, null, null))
                    .hasDescription("This is a test function counter")
                    .hasUnit("items")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(12)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    Metrics.globalRegistry.remove(counter);
    assertThat(testing.collectAllMetrics()).isEmpty();
  }

  @Test
  @SuppressLogger(MetricStorageRegistry.class)
  void functionCountersWithSameNameAndDifferentDescriptions() {
    FunctionCounter.builder("testFunctionCounterWithTags", num, AtomicLong::get)
        .description("First description")
        .tags("tag", "1")
        .baseUnit("items")
        .register(Metrics.globalRegistry);
    FunctionCounter.builder("testFunctionCounterWithTags", anotherNum, AtomicLong::get)
        .description("Second description")
        .tags("tag", "2")
        .baseUnit("items")
        .register(Metrics.globalRegistry);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testFunctionCounterWithTags")
                    .hasDescription("First description")
                    .hasUnit("items")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(12)
                                            .hasAttributes(attributeEntry("tag", "1")))),
            metric ->
                assertThat(metric)
                    .hasName("testFunctionCounterWithTags")
                    .hasDescription("Second description")
                    .hasUnit("items")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(13)
                                            .hasAttributes(attributeEntry("tag", "2")))));
  }
}
