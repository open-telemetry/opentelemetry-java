/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import org.junit.jupiter.api.Test;

class HistogramBucketExemplarReservoirTest {
  @Test
  public void noMeasurement_returnsEmpty() {
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir = new HistogramBucketExemplarReservoir(clock, new double[] {});
    assertThat(reservoir.collectAndReset(Attributes.empty())).isEmpty();
  }

  @Test
  public void oneBucket_samplesEverything() {
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir = new HistogramBucketExemplarReservoir(clock, new double[] {});
    reservoir.offerMeasurement(1L, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar ->
                assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasFilteredAttributes(Attributes.empty())
                    .hasValue(1));
    // Measurement count is reset, we should sample a new measurement (and only one)
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerMeasurement(2L, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar ->
                assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasFilteredAttributes(Attributes.empty())
                    .hasValue(2));
    // only latest measurement is kept per-bucket
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerMeasurement(3L, Attributes.empty(), Context.root());
    reservoir.offerMeasurement(4L, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar ->
                assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasFilteredAttributes(Attributes.empty())
                    .hasValue(4));
  }

  @Test
  public void multipleBuckets_samplesIntoCorrectBucket() {
    TestClock clock = TestClock.create();
    AttributeKey<Long> bucketKey = AttributeKey.longKey("bucket");
    ExemplarReservoir reservoir =
        new HistogramBucketExemplarReservoir(clock, new double[] {0, 10, 20});
    reservoir.offerMeasurement(-1, Attributes.of(bucketKey, 0L), Context.root());
    reservoir.offerMeasurement(1, Attributes.of(bucketKey, 1L), Context.root());
    reservoir.offerMeasurement(11, Attributes.of(bucketKey, 2L), Context.root());
    reservoir.offerMeasurement(21, Attributes.of(bucketKey, 3L), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(4)
        .satisfiesExactlyInAnyOrder(
            e -> assertThat(e).hasValue(-1).hasFilteredAttributes(Attributes.of(bucketKey, 0L)),
            e -> assertThat(e).hasValue(1).hasFilteredAttributes(Attributes.of(bucketKey, 1L)),
            e -> assertThat(e).hasValue(11).hasFilteredAttributes(Attributes.of(bucketKey, 2L)),
            e -> assertThat(e).hasValue(21).hasFilteredAttributes(Attributes.of(bucketKey, 3L)));
  }
}
