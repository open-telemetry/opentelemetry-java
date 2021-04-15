/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

final class DoubleSumAggregator extends AbstractSumAggregator<Double> {
  DoubleSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      AggregationTemporality temporality) {
    super(resource, instrumentationLibraryInfo, descriptor, temporality);
  }

  @Override
  public AggregatorHandle<Double> createHandle() {
    return new Handle();
  }

  @Override
  public Double accumulateDouble(double value) {
    return value;
  }

  @Override
  final Double mergeSum(Double a1, Double a2) {
    return a1 + a2;
  }

  @Override
  final Double mergeDiff(Double previousAccumulation, Double accumulation) {
    return accumulation - previousAccumulation;
  }

  @Override
  public MetricData toMetricData(
      Map<Labels, Double> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricData.createDoubleSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        getInstrumentDescriptor().getName(),
        getInstrumentDescriptor().getDescription(),
        getInstrumentDescriptor().getUnit(),
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

    @Override
    protected Double doAccumulateThenReset() {
      return this.current.sumThenReset();
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
