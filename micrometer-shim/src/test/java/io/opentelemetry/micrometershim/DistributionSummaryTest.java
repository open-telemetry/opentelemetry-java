/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.micrometershim;

import static io.opentelemetry.micrometershim.OpenTelemetryMeterRegistryBuilder.INSTRUMENTATION_NAME;
import static io.opentelemetry.sdk.testing.assertj.MetricAssertions.assertThat;
import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.attributeEntry;

import io.micrometer.core.instrument.DistributionSummary;
import io.micrometer.core.instrument.Metrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

class DistributionSummaryTest {

  @RegisterExtension
  static final MicrometerTestingExtension testing = new MicrometerTestingExtension();

  @Test
  void testMicrometerDistributionSummary() {
    DistributionSummary summary =
        DistributionSummary.builder("testSummary")
            .description("This is a test distribution summary")
            .baseUnit("things")
            .tags("tag", "value")
            .register(Metrics.globalRegistry);

    summary.record(1);
    summary.record(2);
    summary.record(4);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testSummary")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test distribution summary")
                    .hasUnit("things")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(7)
                                .hasCount(3)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testSummary.max")
                    .hasDescription("This is a test distribution summary")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(4)
                                .attributes()
                                .containsEntry("tag", "value")));

    Metrics.globalRegistry.remove(summary);

    // Histogram is synchronous and returns previous value after removal, max is asynchronous and is
    // removed completely.
    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testSummary")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(7)
                                .hasCount(3)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))));
  }

  @Test
  void testMicrometerHistogram() {
    DistributionSummary summary =
        DistributionSummary.builder("testSummary")
            .description("This is a test distribution summary")
            .baseUnit("things")
            .tags("tag", "value")
            .serviceLevelObjectives(1, 10, 100, 1000)
            .distributionStatisticBufferLength(10)
            .register(Metrics.globalRegistry);

    summary.record(0.5);
    summary.record(5);
    summary.record(50);
    summary.record(500);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactlyInAnyOrder(
            metric ->
                assertThat(metric)
                    .hasName("testSummary")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test distribution summary")
                    .hasUnit("things")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(555.5)
                                .hasCount(4)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testSummary.max")
                    .hasDescription("This is a test distribution summary")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(500)
                                .attributes()
                                .containsEntry("tag", "value")),
            metric ->
                assertThat(metric)
                    .hasName("testSummary.histogram")
                    .hasDoubleGauge()
                    .points()
                    .satisfiesExactlyInAnyOrder(
                        point ->
                            assertThat(point).hasValue(1).attributes().containsEntry("le", "1"),
                        point ->
                            assertThat(point).hasValue(2).attributes().containsEntry("le", "10"),
                        point ->
                            assertThat(point).hasValue(3).attributes().containsEntry("le", "100"),
                        point ->
                            assertThat(point)
                                .hasValue(4)
                                .attributes()
                                .containsEntry("le", "1000")));
  }

  @Test
  void testMicrometerPercentiles() {
    DistributionSummary summary =
        DistributionSummary.builder("testSummary")
            .description("This is a test distribution summary")
            .baseUnit("things")
            .tags("tag", "value")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(Metrics.globalRegistry);

    summary.record(50);
    summary.record(100);

    assertThat(testing.collectAllMetrics())
        .satisfiesExactly(
            metric ->
                assertThat(metric)
                    .hasName("testSummary")
                    .hasInstrumentationLibrary(
                        InstrumentationLibraryInfo.create(INSTRUMENTATION_NAME, null))
                    .hasDescription("This is a test distribution summary")
                    .hasUnit("things")
                    .hasDoubleHistogram()
                    .points()
                    .satisfiesExactly(
                        point ->
                            assertThat(point)
                                .hasSum(150)
                                .hasCount(2)
                                .attributes()
                                .containsOnly(attributeEntry("tag", "value"))),
            metric ->
                assertThat(metric)
                    .hasName("testSummary.max")
                    .hasDescription("This is a test distribution summary")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(
                        point ->
                            assertThat(point)
                                .hasValue(100)
                                .attributes()
                                .containsEntry("tag", "value")),
            metric ->
                assertThat(metric)
                    .hasName("testSummary.percentile")
                    .hasDoubleGauge()
                    .points()
                    .anySatisfy(point -> assertThat(point).attributes().containsEntry("phi", "0.5"))
                    .anySatisfy(
                        point -> assertThat(point).attributes().containsEntry("phi", "0.95"))
                    .anySatisfy(
                        point -> assertThat(point).attributes().containsEntry("phi", "0.99")));
  }
}
