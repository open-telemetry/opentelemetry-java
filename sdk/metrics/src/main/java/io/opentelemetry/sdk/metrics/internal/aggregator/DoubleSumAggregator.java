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
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.exemplar.ExemplarReservoir;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.function.Supplier;

final class DoubleSumAggregator extends AbstractSumAggregator<Double> {
  private final Supplier<ExemplarReservoir> reservoirBuilder;

  DoubleSumAggregator(
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
  public AggregatorHandle<Double> createHandle() {
    return new Handle(reservoirBuilder.get());
  }

  @Override
  public Double accumulateDouble(double value) {
    return value;
  }

  @Override
  Double mergeSum(Double previousAccumulation, Double accumulation) {
    return previousAccumulation + accumulation;
  }

  @Override
  Double mergeDiff(Double previousAccumulation, Double accumulation) {
    return accumulation - previousAccumulation;
  }

  @Override
  public MetricData toMetricData(
      Map<Attributes, Double> accumulationByLabels,
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

  static final class Handle extends AggregatorHandle<Double> {
    private final DoubleAdder current = new DoubleAdder();

    Handle(ExemplarReservoir exemplarReservoir) {
      super(exemplarReservoir);
    }

    @Override
    protected Double doAccumulateThenReset(List<Exemplar> exemplars) {
      return this.current.sumThenReset();
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
