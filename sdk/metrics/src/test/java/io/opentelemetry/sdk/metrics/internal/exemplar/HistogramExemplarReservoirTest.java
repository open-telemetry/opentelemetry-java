/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.exemplar;

import static io.opentelemetry.sdk.testing.assertj.OpenTelemetryAssertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.testing.time.TestClock;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collections;
import org.junit.jupiter.api.Test;

class HistogramExemplarReservoirTest {
  @Test
  void noMeasurement_returnsEmpty() {
    TestClock clock = TestClock.create();
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Collections.emptyList());
    assertThat(reservoir.collectAndReset(Attributes.empty())).isEmpty();
  }

  @Test
  void oneBucket_samplesEverything() {
    TestClock clock = TestClock.create();
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Collections.emptyList());
    reservoir.offerDoubleMeasurement(1.1, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(1.1);
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
            });
    // Measurement count is reset, we should sample a new measurement (and only one)
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerDoubleMeasurement(2, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(2);
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
            });
    // only latest measurement is kept per-bucket
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerDoubleMeasurement(3, Attributes.empty(), Context.root());
    reservoir.offerDoubleMeasurement(4, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(3);
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
            });
  }

  @Test
  void multipleBuckets_samplesIntoCorrectBucket() {
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
            e -> {
              assertThat(e.getValue()).isEqualTo(-1.1);
              assertThat(e.getFilteredAttributes()).isEqualTo(Attributes.of(bucketKey, 0L));
            },
            e -> {
              assertThat(e.getValue()).isEqualTo(1);
              assertThat(e.getFilteredAttributes()).isEqualTo(Attributes.of(bucketKey, 1L));
            },
            e -> {
              assertThat(e.getValue()).isEqualTo(11);
              assertThat(e.getFilteredAttributes()).isEqualTo(Attributes.of(bucketKey, 2L));
            },
            e -> {
              assertThat(e.getValue()).isEqualTo(21);
              assertThat(e.getFilteredAttributes()).isEqualTo(Attributes.of(bucketKey, 3L));
            });
  }

  @Test
  void longMeasurement_CastsToDouble() {
    TestClock clock = TestClock.create();
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Collections.emptyList());
    reservoir.offerLongMeasurement(1L, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(1);
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
            });
    // Measurement count is reset, we should sample a new measurement (and only one)
    clock.advance(Duration.ofSeconds(1));
    reservoir.offerLongMeasurement(2, Attributes.empty(), Context.root());
    assertThat(reservoir.collectAndReset(Attributes.empty()))
        .hasSize(1)
        .satisfiesExactly(
            exemplar -> {
              assertThat(exemplar.getEpochNanos()).isEqualTo(clock.now());
              assertThat(exemplar.getValue()).isEqualTo(2);
              assertThat(exemplar.getFilteredAttributes()).isEmpty();
            });
  }

  @Test
  public void multiMeasurement_setSpanAttributeOnFirstMeasurement() {
    SpanBuilder spanBuilder = SdkTracerProvider.builder().build().get("").spanBuilder("");
    Span span = spy(spanBuilder.startSpan());
    Context ctx = Context.root().with(span);
    TestClock clock = TestClock.create();
    ExemplarReservoir<DoubleExemplarData> reservoir =
        new HistogramExemplarReservoir(clock, Collections.emptyList());
    reservoir.offerLongMeasurement(1, Attributes.empty(), ctx);
    verify(span).setAttribute(AttributeKey.booleanKey("otel.exemplar"), true);
    reservoir.offerLongMeasurement(2, Attributes.empty(), ctx);
    verify(span, never()).setAttribute(any(), anyBoolean());
  }
}
