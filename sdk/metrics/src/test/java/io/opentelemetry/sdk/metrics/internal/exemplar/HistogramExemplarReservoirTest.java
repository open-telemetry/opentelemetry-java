/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.testing.assertj.MetricAssertions;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class HistogramExemplarReservoirTest {
  @Test
  public void noMeasurement_returnsEmpty() {
    TestClock clock = TestClock.create();
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Collections.emptyList());
    assertThat(reservoir.collectAndReset(Attributes.empty())).isEmpty();
  }

  @Test
  public void oneBucket_samplesEverything() {
    TestClock clock = TestClock.create();
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Collections.emptyList());
    reservoir.offerDoubleMeasurement(1.1, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar ->
                MetricAssertions.assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasFilteredAttributes(Attributes.empty())
                    .hasValue(1.1));
    // Measurement count is reset, we should sample a new measurement (and only one)
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerDoubleMeasurement(2, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar ->
                MetricAssertions.assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasFilteredAttributes(Attributes.empty())
                    .hasValue(2));
    // only latest measurement is kept per-bucket
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerDoubleMeasurement(3, Attributes.empty(), Context.root());
    reservoir.offerDoubleMeasurement(4, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar ->
                MetricAssertions.assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasFilteredAttributes(Attributes.empty())
                    .hasValue(4));
  }

  @Test
  public void multipleBuckets_samplesIntoCorrectBucket() {
    TestClock clock = TestClock.create();
    AttributeKey<Long> bucketKey = AttributeKey.longKey("bucket");
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Arrays.asList(0d, 10d, 20d));
    reservoir.offerDoubleMeasurement(-1.1, Attributes.of(bucketKey, 0L), Context.root());
    reservoir.offerDoubleMeasurement(1, Attributes.of(bucketKey, 1L), Context.root());
    reservoir.offerDoubleMeasurement(11, Attributes.of(bucketKey, 2L), Context.root());
    reservoir.offerDoubleMeasurement(21, Attributes.of(bucketKey, 3L), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(4)
        .satisfiesExactlyInAnyOrder(
            e ->
                MetricAssertions.assertThat(e)
                    .hasValue(-1.1)
                    .hasFilteredAttributes(Attributes.of(bucketKey, 0L)),
            e ->
                MetricAssertions.assertThat(e)
                    .hasValue(1)
                    .hasFilteredAttributes(Attributes.of(bucketKey, 1L)),
            e ->
                MetricAssertions.assertThat(e)
                    .hasValue(11)
                    .hasFilteredAttributes(Attributes.of(bucketKey, 2L)),
            e ->
                MetricAssertions.assertThat(e)
                    .hasValue(21)
                    .hasFilteredAttributes(Attributes.of(bucketKey, 3L)));
  }
}
