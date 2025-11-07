/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.internal.RandomSupplier;
import io.opentelemetry.sdk.testing.time.TestClock;
import java.time.Duration;
import java.util.Random;
import org.junit.jupiter.api.Test;

class RandomFixedSizeExemplarReservoirTest {
  private static final String TRACE_ID = "ff000000000000000000000000000041";
  private static final String SPAN_ID = "ff00000000000041";

  @Test
  public void noMeasurement_returnsEmpty() {
    TestClock clock = TestClock.create();
    RandomFixedSizeExemplarReservoir reservoir =
        RandomFixedSizeExemplarReservoir.create(clock, 1, RandomSupplier.platformDefault());
    assertThat(reservoir.collectAndResetDoubles(Attributes.empty())).isEmpty();
  }

  @Test
  public void oneMeasurement_alwaysSamplesFirstMeasurement() {
    TestClock clock = TestClock.create();
    RandomFixedSizeExemplarReservoir reservoir =
        RandomFixedSizeExemplarReservoir.create(clock, 1, RandomSupplier.platformDefault());
    reservoir.offerDoubleMeasurement(1.1, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndResetDoubles(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
              assertThat(exemplar.getValue()).isEqualTo(1.1);
            });

    // Measurement count is reset, we should sample a new measurement (and only one)
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerDoubleMeasurement(2, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndResetDoubles(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
              assertThat(exemplar.getValue()).isEqualTo(2);
            });
  }

  @Test
  public void oneMeasurement_filtersAttributes() {
    Attributes all =
        Attributes.builder().put("one", 1).put("two", "two").put("three", true).build();
    Attributes partial = Attributes.builder().put("three", true).build();
    Attributes remaining = Attributes.builder().put("one", 1).put("two", "two").build();
    TestClock clock = TestClock.create();
    RandomFixedSizeExemplarReservoir reservoir =
        RandomFixedSizeExemplarReservoir.create(clock, 1, RandomSupplier.platformDefault());
    reservoir.offerDoubleMeasurement(1.1, all, Context.root());
    assertThat(reservoir.collectAndResetDoubles(partial))
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(1.1);
              assertThat(exemplar.getFilteredAttributes()).isEqualTo(remaining);
            });
  }

  @Test
  public void oneMeasurement_includesTraceAndSpanIds() {
    Attributes all =
        Attributes.builder().put("one", 1).put("two", "two").put("three", true).build();
    Context context =
        Context.root()
            .with(
                Span.wrap(
                    SpanContext.createFromRemoteParent(
                        TRACE_ID, SPAN_ID, TraceFlags.getSampled(), TraceState.getDefault())));
    TestClock clock = TestClock.create();
    RandomFixedSizeExemplarReservoir reservoir =
        RandomFixedSizeExemplarReservoir.create(clock, 1, RandomSupplier.platformDefault());
    reservoir.offerDoubleMeasurement(1, all, context);
    assertThat(reservoir.collectAndResetDoubles(Attributes.empty()))
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(1);
              assertThat(exemplar.getFilteredAttributes()).isEqualTo(all);
              assertThat(exemplar.getSpanContext().getTraceId()).isEqualTo(TRACE_ID);
              assertThat(exemplar.getSpanContext().getSpanId()).isEqualTo(SPAN_ID);
            });
  }

  @Test
  public void multiMeasurements_preservesLatestSamples() {
    AttributeKey<Long> key = AttributeKey.longKey("K");
    // We cannot mock random in latest jdk, so we create an override.
    Random mockRandom =
        new Random() {
          @Override
          public int nextInt(int max) {
            switch (max) {
              // Force one sample in bucket 1 and two in bucket 0.
              case 2:
                return 1;
              default:
                return 0;
            }
          }
        };
    TestClock clock = TestClock.create();
    DoubleExemplarReservoir reservoir =
        ExemplarReservoirFactory.fixedSizeReservoir(clock, 2, () -> mockRandom)
            .createDoubleExemplarReservoir();
    reservoir.offerDoubleMeasurement(1, Attributes.of(key, 1L), Context.root());
    reservoir.offerDoubleMeasurement(2, Attributes.of(key, 2L), Context.root());
    reservoir.offerDoubleMeasurement(3, Attributes.of(key, 3L), Context.root());
    assertThat(reservoir.collectAndResetDoubles(Attributes.empty()))
        .satisfiesExactlyInAnyOrder(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(2);
            },
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(3);
            });
  }
}
