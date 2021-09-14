/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.exemplar;

import static io.opentelemetry.sdk.testing.assertj.metrics.MetricAssertions.assertThat;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.common.RandomHolder;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;

public class FixedSizeExemplarReservoirTest {
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String SPAN_ID = "ff00000000000041";

  @Test
  public void noMeasurement_returnsEmpty() {
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir =
        new FixedSizeExemplarReservoir(clock, 1, RandomHolder.platformDefault());
    assertThat(reservoir.collectAndReset(Attributes.empty())).isEmpty();
  }

  @Test
  public void oneMeasurement_alwaysSamplesFirstMeasurement() {
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir =
        new FixedSizeExemplarReservoir(clock, 1, RandomHolder.platformDefault());
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
  }

  @Test
  public void oneMeasurement_filtersAttributes() {
    Attributes all =
        Attributes.builder().put("one", 1).put("two", "two").put("three", true).build();
    Attributes partial = Attributes.builder().put("three", true).build();
    Attributes remaining = Attributes.builder().put("one", 1).put("two", "two").build();
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir =
        new FixedSizeExemplarReservoir(clock, 1, RandomHolder.platformDefault());
    reservoir.offerMeasurement(1L, all, Context.root());
    assertThat(reservoir.collectAndReset(partial))
        .satisfiesExactly(
            exemplar ->
                assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasValue(1)
                    .hasFilteredAttributes(remaining));
  }

  @Test
  public void oneMeasurement_includesTraceAndSpanIds() {
    Attributes all =
        Attributes.builder().put("one", 1).put("two", "two").put("three", true).build();
    final Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault())));
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir =
        new FixedSizeExemplarReservoir(clock, 1, RandomHolder.platformDefault());
    reservoir.offerMeasurement(1L, all, context);
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .satisfiesExactly(
            exemplar ->
                assertThat(exemplar)
                    .hasEpochNanos(clock.now())
                    .hasValue(1)
                    .hasFilteredAttributes(all)
                    .hasTraceId(TRACE_ID)
                    .hasSpanId(SPAN_ID));
  }

  @Test
  public void multiMeasurements_preservesLatestSamples() {
    AttributeKey<Long> K = AttributeKey.longKey("K");
    Random mockRandom = mock(Random.class);
    TestClock clock = TestClock.create();
    ExemplarReservoir reservoir =
        new FixedSizeExemplarReservoir(clock, 2, RandomHolder.create(mockRandom));
    // Force one sample in bucket 1 and two in bucket 0.
    when(mockRandom.nextInt(1)).thenReturn(0);
    when(mockRandom.nextInt(2)).thenReturn(1);
    when(mockRandom.nextInt(3)).thenReturn(0);
    reservoir.offerMeasurement(1, Attributes.of(K, 1L), Context.root());
    reservoir.offerMeasurement(2, Attributes.of(K, 2L), Context.root());
    reservoir.offerMeasurement(3, Attributes.of(K, 3L), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .satisfiesExactlyInAnyOrder(
            exemplar -> assertThat(exemplar).hasEpochNanos(clock.now()).hasValue(2),
            exemplar -> assertThat(exemplar).hasEpochNanos(clock.now()).hasValue(3));
  }
}
