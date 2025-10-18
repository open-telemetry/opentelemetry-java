/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.util.concurrent.AtomicDouble;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoubleExemplarData;
import io.opentelemetry.sdk.metrics.internal.exemplar.DoubleExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoirFactory;
import io.opentelemetry.sdk.metrics.internal.exemplar.LongExemplarReservoir;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AggregatorHandleTest {

  @Mock DoubleExemplarReservoir doubleReservoir;
  @Mock LongExemplarReservoir longReservoir;

  @Test
  void testRecordings() {
    TestLongAggregatorHandle testLongAggregator = new TestLongAggregatorHandle(longReservoir);

    testLongAggregator.recordLong(22, Attributes.empty(), Context.current());
    assertThat(testLongAggregator.recordedLong.get()).isEqualTo(22);

    testLongAggregator.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(testLongAggregator.recordedLong.get()).isEqualTo(0);

    TestDoubleAggregatorHandle testDoubleAggregator =
        new TestDoubleAggregatorHandle(doubleReservoir);

    testDoubleAggregator.recordDouble(33.55, Attributes.empty(), Context.current());
    assertThat(testDoubleAggregator.recordedDouble.get()).isEqualTo(33.55);

    testDoubleAggregator.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(testDoubleAggregator.recordedDouble.get()).isEqualTo(0);
  }

  @Test
  void testOfferMeasurementLongToExemplar() {
    TestLongAggregatorHandle testAggregator = new TestLongAggregatorHandle(longReservoir);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Context context = Context.root();
    testAggregator.recordLong(1L, attributes, context);
    Mockito.verify(longReservoir).offerLongMeasurement(1L, attributes, context);
  }

  @Test
  void testOfferMeasurementDoubleToExemplar() {
    TestDoubleAggregatorHandle testAggregator = new TestDoubleAggregatorHandle(doubleReservoir);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Context context = Context.root();
    testAggregator.recordDouble(1.0d, attributes, context);
    Mockito.verify(doubleReservoir).offerDoubleMeasurement(1.0d, attributes, context);
  }

  @Test
  void testGenerateExemplarsOnCollect() {
    TestDoubleAggregatorHandle testAggregator = new TestDoubleAggregatorHandle(doubleReservoir);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    DoubleExemplarData result =
        ImmutableDoubleExemplarData.create(
            attributes,
            2L,
            SpanContext.create(
                "00000000000000000000000000000001",
                "0000000000000002",
                TraceFlags.getDefault(),
                TraceState.getDefault()),
            1);
    // We need to first record a value so that collect and reset does something.
    testAggregator.recordDouble(1.0, Attributes.empty(), Context.root());
    Mockito.when(doubleReservoir.collectAndResetDoubles(attributes))
        .thenReturn(Collections.singletonList(result));
    testAggregator.aggregateThenMaybeReset(0, 1, attributes, /* reset= */ true);
    assertThat(testAggregator.recordedExemplars.get()).isEqualTo(Collections.singletonList(result));
  }

  private static class TestDoubleAggregatorHandle extends TestAggregatorHandle {

    TestDoubleAggregatorHandle(DoubleExemplarReservoir reservoir) {
      super(reservoir, null);
    }

    @Override
    protected boolean isDoubleType() {
      return true;
    }
  }

  private static class TestLongAggregatorHandle extends TestAggregatorHandle {

    TestLongAggregatorHandle(LongExemplarReservoir reservoir) {
      super(null, reservoir);
    }

    @Override
    protected boolean isDoubleType() {
      return false;
    }
  }

  private abstract static class TestAggregatorHandle extends AggregatorHandle<PointData> {
    final AtomicLong recordedLong = new AtomicLong();
    final AtomicDouble recordedDouble = new AtomicDouble();
    final AtomicReference<List<? extends ExemplarData>> recordedExemplars = new AtomicReference<>();

    TestAggregatorHandle(
        @Nullable DoubleExemplarReservoir doubleReservoir,
        @Nullable LongExemplarReservoir longReservoir) {
      super(
          new ExemplarReservoirFactory() {
            @Override
            public DoubleExemplarReservoir createDoubleExemplarReservoir() {
              return Objects.requireNonNull(doubleReservoir);
            }

            @Override
            public LongExemplarReservoir createLongExemplarReservoir() {
              return Objects.requireNonNull(longReservoir);
            }
          });
    }

    @Nullable
    @Override
    protected PointData doAggregateThenMaybeResetDoubles(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      recordedLong.set(0);
      recordedDouble.set(0);
      recordedExemplars.set(exemplars);
      return null;
    }

    @Nullable
    @Override
    protected PointData doAggregateThenMaybeResetLongs(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<LongExemplarData> exemplars,
        boolean reset) {
      recordedLong.set(0);
      recordedDouble.set(0);
      recordedExemplars.set(exemplars);
      return null;
    }

    @Override
    protected void doRecordLong(long value) {
      recordedLong.set(value);
    }

    @Override
    protected void doRecordDouble(double value) {
      recordedDouble.set(value);
    }
  }
}
