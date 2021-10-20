/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataBuilder;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;
import java.util.function.Supplier;

final class LongSumAggregator extends AbstractSumAggregator<LongAccumulation> {

  private final Supplier<ExemplarReservoir> reservoirSupplier;

  LongSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      AggregationTemporality temporality,
      Supplier<ExemplarReservoir> reservoirSupplier) {
    super(
        resource, instrumentationLibraryInfo, instrumentDescriptor, metricDescriptor, temporality);
    this.reservoirSupplier = reservoirSupplier;
  }

  @Override
  public AggregatorHandle<LongAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public LongAccumulation accumulateLong(long value) {
    return LongAccumulation.create(value);
  }

  @Override
  LongAccumulation mergeSum(LongAccumulation previousAccumulation, LongAccumulation accumulation) {
    return LongAccumulation.create(
        previousAccumulation.getValue() + accumulation.getValue(), accumulation.getExemplars());
  }

  @Override
  LongAccumulation mergeDiff(LongAccumulation previousAccumulation, LongAccumulation accumulation) {
    return LongAccumulation.create(
        accumulation.getValue() - previousAccumulation.getValue(), accumulation.getExemplars());
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, LongAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    MetricDescriptor descriptor = getMetricDescriptor();
    return MetricDataBuilder.createLongSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        LongSumData.create(
            isMonotonic(),
            temporality(),
            MetricDataUtils.toLongPointList(
                accumulationByLabels,
                temporality() == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
  }

  static final class Handle extends AggregatorHandle<LongAccumulation> {
    private final LongAdder current = new LongAdder();

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected LongAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      return LongAccumulation.create(this.current.sumThenReset(), exemplars);
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
