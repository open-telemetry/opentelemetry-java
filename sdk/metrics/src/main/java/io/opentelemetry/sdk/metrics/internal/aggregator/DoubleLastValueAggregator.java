/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.MutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

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
@ThreadSafe
public final class DoubleLastValueAggregator
    implements Aggregator<DoublePointData, DoubleExemplarData> {
  private final Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier;
  private final MemoryMode memoryMode;

  public DoubleLastValueAggregator(
      Supplier<ExemplarReservoir<DoubleExemplarData>> reservoirSupplier, MemoryMode memoryMode) {
    this.reservoirSupplier = reservoirSupplier;
    this.memoryMode = memoryMode;
  }

  @Override
  public AggregatorHandle<DoublePointData, DoubleExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), memoryMode);
  }

  @Override
  public DoublePointData diff(DoublePointData previous, DoublePointData current) {
    return current;
  }

  @Override
  public void diffInPlace(DoublePointData previousReusable, DoublePointData current) {
    ((MutableDoublePointData) previousReusable).set(current);
  }

  @Override
  public DoublePointData createReusablePoint() {
    return new MutableDoublePointData();
  }

  @Override
  public void copyPoint(DoublePointData point, DoublePointData toReusablePoint) {
    ((MutableDoublePointData) toReusablePoint).set(point);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationScopeInfo instrumentationScopeInfo,
      MetricDescriptor descriptor,
      Collection<DoublePointData> points,
      AggregationTemporality temporality) {
    // Gauge does not need a start time, but we send one as advised by the data model
    // for identifying resets.
    return ImmutableMetricData.createDoubleGauge(
        resource,
        instrumentationScopeInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getSourceInstrument().getUnit(),
        ImmutableGaugeData.create(points));
  }

  static final class Handle extends AggregatorHandle<DoublePointData, DoubleExemplarData> {
    private final AtomicReference<AtomicLong> current = new AtomicReference<>(null);
    private final AtomicLong valueBits = new AtomicLong();

    // Only used when memoryMode is REUSABLE_DATA
    @Nullable private final MutableDoublePointData reusablePoint;

    private Handle(ExemplarReservoir<DoubleExemplarData> reservoir, MemoryMode memoryMode) {
      super(reservoir);
      if (memoryMode == MemoryMode.REUSABLE_DATA) {
        reusablePoint = new MutableDoublePointData();
      } else {
        reusablePoint = null;
      }
    }

    @Override
    protected DoublePointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<DoubleExemplarData> exemplars,
        boolean reset) {
      AtomicLong valueBits =
          Objects.requireNonNull(reset ? this.current.getAndSet(null) : this.current.get());
      double value = Double.longBitsToDouble(valueBits.get());
      if (reusablePoint != null) {
        reusablePoint.set(startEpochNanos, epochNanos, attributes, value, exemplars);
        return reusablePoint;
      } else {
        return ImmutableDoublePointData.create(
            startEpochNanos, epochNanos, attributes, value, exemplars);
      }
    }

    @Override
    protected void doRecordDouble(double value) {
      valueBits.set(Double.doubleToLongBits(value));
      current.compareAndSet(null, valueBits);
    }
  }
}
