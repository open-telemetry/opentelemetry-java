/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

final class DoubleSumAggregator extends AbstractSumAggregator<DoubleAccumulation> {
  private final Supplier<ExemplarReservoir> reservoirSupplier;

  DoubleSumAggregator(
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
  public AggregatorHandle<DoubleAccumulation> createHandle() {
    return new Handle(reservoirSupplier.get());
  }

  @Override
  public DoubleAccumulation accumulateDouble(double value) {
    return DoubleAccumulation.create(value);
  }

  @Override
  DoubleAccumulation mergeSum(
      DoubleAccumulation previousAccumulation, DoubleAccumulation accumulation) {
    return DoubleAccumulation.create(
        previousAccumulation.getValue() + accumulation.getValue(), accumulation.getExemplars());
  }

  @Override
  DoubleAccumulation mergeDiff(
      DoubleAccumulation previousAccumulation, DoubleAccumulation accumulation) {
    return DoubleAccumulation.create(
        accumulation.getValue() - previousAccumulation.getValue(), accumulation.getExemplars());
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, DoubleAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
        getMetricDescriptor().getUnit(),
        DoubleSumData.create(
            isMonotonic(),
            temporality(),
            MetricDataUtils.toDoublePointList(
                accumulationByLabels,
                temporality() == AggregationTemporality.CUMULATIVE
                    ? startEpochNanos
                    : lastCollectionEpoch,
                epochNanos)));
  }

  static final class Handle extends AggregatorHandle<DoubleAccumulation> {
    private final DoubleAdder current = new DoubleAdder();

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected DoubleAccumulation doAccumulateThenReset(List<ExemplarData> exemplars) {
      return DoubleAccumulation.create(this.current.sumThenReset(), exemplars);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
