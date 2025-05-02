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
import io.opentelemetry.sdk.metrics.internal.concurrent.AdderUtil;
import io.opentelemetry.sdk.metrics.internal.concurrent.LongAdder;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.MutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.metrics.internal.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Sum aggregator that keeps values as {@code long}s.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LongSumAggregator
    extends AbstractSumAggregator<LongPointData, LongExemplarData> {

  private final Supplier<ExemplarReservoir<LongExemplarData>> reservoirSupplier;
  private final MemoryMode memoryMode;

  public LongSumAggregator(
      InstrumentDescriptor instrumentDescriptor,
      Supplier<ExemplarReservoir<LongExemplarData>> reservoirSupplier,
      MemoryMode memoryMode) {
    super(instrumentDescriptor);
    this.reservoirSupplier = reservoirSupplier;
    this.memoryMode = memoryMode;
  }

  @Override
  public AggregatorHandle<LongPointData, LongExemplarData> createHandle() {
    return new Handle(reservoirSupplier.get(), memoryMode);
  }

  @Override
  public LongPointData diff(LongPointData previousPoint, LongPointData currentPoint) {
    return ImmutableLongPointData.create(
        currentPoint.getStartEpochNanos(),
        currentPoint.getEpochNanos(),
        currentPoint.getAttributes(),
        currentPoint.getValue() - previousPoint.getValue(),
        currentPoint.getExemplars());
  }

  @Override
  public void diffInPlace(LongPointData previousReusablePoint, LongPointData currentPoint) {
    ((MutableLongPointData) previousReusablePoint)
        .set(
            currentPoint.getStartEpochNanos(),
            currentPoint.getEpochNanos(),
            currentPoint.getAttributes(),
            currentPoint.getValue() - previousReusablePoint.getValue(),
            currentPoint.getExemplars());
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
    return ImmutableMetricData.createLongSum(
        resource,
        instrumentationScopeInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getSourceInstrument().getUnit(),
        ImmutableSumData.create(isMonotonic(), temporality, points));
  }

  static final class Handle extends AggregatorHandle<LongPointData, LongExemplarData> {
    private final LongAdder current = AdderUtil.createLongAdder();

    // Only used if memoryMode == MemoryMode.REUSABLE_DATA
    @Nullable private final MutableLongPointData reusablePointData;

    Handle(ExemplarReservoir<LongExemplarData> exemplarReservoir, MemoryMode memoryMode) {
      super(exemplarReservoir);
      reusablePointData =
          memoryMode == MemoryMode.REUSABLE_DATA ? new MutableLongPointData() : null;
    }

    @Override
    protected LongPointData doAggregateThenMaybeReset(
        long startEpochNanos,
        long epochNanos,
        Attributes attributes,
        List<LongExemplarData> exemplars,
        boolean reset) {
      long value = reset ? this.current.sumThenReset() : this.current.sum();
      if (reusablePointData != null) {
        reusablePointData.set(startEpochNanos, epochNanos, attributes, value, exemplars);
        return reusablePointData;
      } else {
        return ImmutableLongPointData.create(
            startEpochNanos, epochNanos, attributes, value, exemplars);
      }
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
