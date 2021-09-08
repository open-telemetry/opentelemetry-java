/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.Exemplar;
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
final class CountAggregator extends AbstractAggregator<LongAccumulation> {
  private final AggregationTemporality temporality;
  // Workaround
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  CountAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor descriptor,
      AggregationTemporality temporality,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    super(
        resource,
        instrumentationLibraryInfo,
        descriptor,
        temporality == AggregationTemporality.CUMULATIVE);
    this.temporality = temporality;
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public LongAccumulation accumulateDouble(double value) {
    return LongAccumulation.create(1L);
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(1L);
  }

  @Override
  public LongAccumulation merge(LongAccumulation a1, LongAccumulation a2) {
    return LongAccumulation.create(a1.getValue() + a2.getValue(), a2.getExemplars());
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, LongAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createLongSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
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
    protected LongAccumulation doAccumulateThenReset(List<Exemplar> exemplars) {
      return LongAccumulation.create(current.sumThenReset(), exemplars);
    }
  }
}
