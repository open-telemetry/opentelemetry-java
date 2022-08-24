/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometer1shim;

import static io.opentelemetry.micrometer1shim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.Measurement;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Statistic;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class MeterTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  @Test
  void testMeter() {
    AtomicReference<Double> number = new AtomicReference<>(12345.0);

    List<Measurement> measurements =
        Arrays.asList(
            new Measurement(number::get, Statistic.TOTAL),
            new Measurement(number::get, Statistic.TOTAL_TIME),
            new Measurement(number::get, Statistic.COUNT),
            new Measurement(number::get, Statistic.ACTIVE_TASKS),
            new Measurement(number::get, Statistic.DURATION),
            new Measurement(number::get, Statistic.MAX),
            new Measurement(number::get, Statistic.VALUE),
            new Measurement(number::get, Statistic.UNKNOWN));

    Meter meter =
        Meter.builder("testMeter", Meter.Type.OTHER, measurements)
            .description("This is a test meter")
            .baseUnit("things")
            .tag("tag", "value")
            .register(Metrics.globalRegistry);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testMeter.total")
                    .hasInstrumentationScope(InstrumentationScopeInfo.create(INSTRUMENTATION_NAME))
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(12345)
                                            .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.total_time")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(12345)
                                            .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.count")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(12345)
                                            .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.active")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleSumSatisfying(
                        sum ->
                            sum.isNotMonotonic()
                                .hasPointsSatisfying(
                                    point ->
                                        point
                                            .hasValue(12345)
                                            .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.duration")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(12345)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.max")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(12345)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.value")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(12345)
                                        .hasAttributes(attributeEntry("tag", "value")))),
            metric ->
                assertThat(metric)
                    .hasName("testMeter.unknown")
                    .hasDescription("This is a test meter")
                    .hasUnit("things")
                    .hasDoubleGaugeSatisfying(
                        gauge ->
                            gauge.hasPointsSatisfying(
                                point ->
                                    point
                                        .hasValue(12345)
                                        .hasAttributes(attributeEntry("tag", "value")))));

    // when
    Metrics.globalRegistry.remove(meter);
    assertThat(testing.collectAllMetrics()).isEmpty();
  }
}
