/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
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

final class LongSumAggregator extends AbstractSumAggregator<Long> {

  private final Supplier<ExemplarReservoir> reservoirBuilder;

  LongSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor instrumentDescriptor,
      MetricDescriptor metricDescriptor,
      AggregationTemporality temporality,
      Supplier<ExemplarReservoir> reservoirBuilder) {
    super(
        resource, instrumentationLibraryInfo, instrumentDescriptor, metricDescriptor, temporality);
    this.reservoirBuilder = reservoirBuilder;
  }

  @Override
  public AggregatorHandle<Long> createHandle() {
    return new Handle(reservoirBuilder.get());
  }

  @Override
  public Long accumulateLong(long value) {
    return value;
  }

  @Override
  Long mergeSum(Long previousAccumulation, Long accumulation) {
    return previousAccumulation + accumulation;
  }

  @Override
  Long mergeDiff(Long previousAccumulation, Long accumulation) {
    return accumulation - previousAccumulation;
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, Long> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    MetricDescriptor descriptor = getMetricDescriptor();
    return MetricData.createLongSum(
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

  static final class Handle extends AggregatorHandle<Long> {
    private final LongAdder current = new LongAdder();

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected Long doAccumulateThenReset(List<Exemplar> exemplars) {
      return this.current.sumThenReset();
    }

    @Override
    public void doRecordLong(long value) {
      current.add(value);
    }
  }
}
