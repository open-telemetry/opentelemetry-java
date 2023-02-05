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
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import java.util.Collections;
import java.util.List;
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

  @Mock ExemplarReservoir<DoubleExemplarData> doubleReservoir;
  @Mock ExemplarReservoir<LongExemplarData> longReservoir;

  @Test
  void testRecordings() {
    TestAggregatorHandle<?> testAggregator = new TestAggregatorHandle<>(doubleReservoir);

    testAggregator.recordLong(22);
    assertThat(testAggregator.recordedLong.get()).isEqualTo(22);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);

    testAggregator.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);

    testAggregator.recordDouble(33.55);
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(33.55);

    testAggregator.aggregateThenMaybeReset(0, 1, Attributes.empty(), /* reset= */ true);
    assertThat(testAggregator.recordedLong.get()).isEqualTo(0);
    assertThat(testAggregator.recordedDouble.get()).isEqualTo(0);
  }

  @Test
  void testOfferMeasurementLongToExemplar() {
    TestAggregatorHandle<?> testAggregator = new TestAggregatorHandle<>(longReservoir);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Context context = Context.root();
    testAggregator.recordLong(1L, attributes, context);
    Mockito.verify(longReservoir).offerLongMeasurement(1L, attributes, context);
  }

  @Test
  void testOfferMeasurementDoubleToExemplar() {
    TestAggregatorHandle<?> testAggregator = new TestAggregatorHandle<>(doubleReservoir);
    Attributes attributes = Attributes.builder().put("test", "value").build();
    Context context = Context.root();
    testAggregator.recordDouble(1.0d, attributes, context);
    Mockito.verify(doubleReservoir).offerDoubleMeasurement(1.0d, attributes, context);
  }

  @Test
  void testGenerateExemplarsOnCollect() {
    TestAggregatorHandle<DoubleExemplarData> testAggregator =
        new TestAggregatorHandle<>(doubleReservoir);
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
    Mockito.when(doubleReservoir.collectAndReset(attributes))
        .thenReturn(Collections.singletonList(result));
    testAggregator.aggregateThenMaybeReset(0, 1, attributes, /* reset= */ true);
    assertThat(testAggregator.recordedExemplars.get()).containsExactly(result);
  }

  private static class TestAggregatorHandle<T extends ExemplarData>
      extends AggregatorHandle<PointData, T> {
    final AtomicLong recordedLong = new AtomicLong();
    final AtomicDouble recordedDouble = new AtomicDouble();
    final AtomicReference<List<T>> recordedExemplars = new AtomicReference<>();

    TestAggregatorHandle(ExemplarReservoir<T> reservoir) {
      super(reservoir);
    }

    @Nullable
    @Override
    protected PointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<T> exemplars,
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
