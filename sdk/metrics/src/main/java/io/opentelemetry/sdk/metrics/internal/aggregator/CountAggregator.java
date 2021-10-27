/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;
import javax.annotation.concurrent.ThreadSafe;

@ThreadSafe
final class CountAggregator implements Aggregator<LongAccumulation> {
  // Workaround
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  CountAggregator(Supplier<ExemplarReservoir> reservoirSupplier) {
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public LongAccumulation merge(LongAccumulation previous, LongAccumulation current) {
    return LongAccumulation.create(
        previous.getValue() + current.getValue(), current.getExemplars());
  }

  @Override
  public LongAccumulation diff(LongAccumulation previous, LongAccumulation current) {
    // For count of measurements, `diff` returns the "DELTA" of measurements that occurred.
    // Given how we aggregate, this effectively is just the current value for async
    // instruments.
    return current;
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibrary,
      MetricDescriptor metricDescriptor,
      Map<Attributes, LongAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createLongSum(
        resource,
        instrumentationLibrary,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        "1",
        LongSumData.create(
            /* isMonotonic= */ true,
            temporality,
            MetricDataUtils.toLongPointList(
                accumulationByLabels,
                temporality == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    private final LongAdder current = new LongAdder();

    private Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected void doRecordLong(long value) {
      current.add(1);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(1);
    }

    @Override
    protected LongAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      return LongAccumulation.create(current.sumThenReset(), exemplars);
    }
  }
}
