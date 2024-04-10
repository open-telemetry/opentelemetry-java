/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.MutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.state.Measurement;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LongLastValueAggregator implements Aggregator<LongPointData, LongExemplarData> {
  private final Supplier<ExemplarReservoir<LongExemplarData>> reservoirSupplier;
  private final MemoryMode memoryMode;

  public LongLastValueAggregator(
      Supplier<ExemplarReservoir<LongExemplarData>> reservoirSupplier, MemoryMode memoryMode) {
    this.reservoirSupplier = reservoirSupplier;
    this.memoryMode = memoryMode;
  }

  @Override
  public AggregatorHandle<LongPointData, LongExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), memoryMode);
  }

  @Override
  public LongPointData diff(LongPointData previous, LongPointData current) {
    return current;
  }

  @Override
  public void diffInPlace(LongPointData previousReusablePoint, LongPointData currentPoint) {
    ((MutableLongPointData) previousReusablePoint).set(currentPoint);
  }

  @Override
  public LongPointData toPoint(Measurement measurement) {
    return ImmutableLongPointData.create(
        measurement.startEpochNanos(),
        measurement.epochNanos(),
        measurement.attributes(),
        measurement.longValue());
  }

  @Override
  public void toPoint(Measurement measurement, LongPointData reusablePoint) {
    ((MutableLongPointData) reusablePoint)
        .set(
            measurement.startEpochNanos(),
            measurement.epochNanos(),
            measurement.attributes(),
            measurement.longValue());
  }

  @Override
  public LongPointData createReusablePoint() {
    return new MutableLongPointData();
  }

  @Override
  public void copyPoint(LongPointData point, LongPointData toReusablePoint) {
    ((MutableLongPointData) toReusablePoint).set(point);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      Collection<LongPointData> points,
      AggregationTemporality temporality) {
    // Last-Value ignores temporality generally, but we can set a start time on the gauge.
    return ImmutableMetricData.createLongGauge(
        resource,
        instrumentationScopeInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getSourceInstrument().getUnit(),
        ImmutableGaugeData.create(points));
  }

  static final class Handle extends AggregatorHandle<LongPointData, LongExemplarData> {
    @Nullable private static final Long DEFAULT_VALUE = null;
    private final AtomicReference<Long> current = new AtomicReference<>(DEFAULT_VALUE);

    // Only used when memoryMode is REUSABLE_DATA
    @Nullable private final MutableLongPointData reusablePoint;

    Handle(ExemplarReservoir<LongExemplarData> exemplarReservoir, MemoryMode memoryMode) {
      super(exemplarReservoir);
      if (memoryMode == MemoryMode.REUSABLE_DATA) {
        reusablePoint = new MutableLongPointData();
      } else {
        reusablePoint = null;
      }
    }

    @Override
    protected LongPointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<LongExemplarData> exemplars,
        boolean reset) {
      Long value = reset ? this.current.getAndSet(DEFAULT_VALUE) : this.current.get();

      if (reusablePoint != null) {
        reusablePoint.set(
            startEpochNanos, epochNanos, attributes, Objects.requireNonNull(value), exemplars);
        return reusablePoint;
      } else {
        return ImmutableLongPointData.create(
            startEpochNanos, epochNanos, attributes, Objects.requireNonNull(value), exemplars);
      }
    }

    @Override
    protected void doRecordLong(long value) {
      current.set(value);
    }
  }
}
