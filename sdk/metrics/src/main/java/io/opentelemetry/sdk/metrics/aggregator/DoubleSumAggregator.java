/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

final class DoubleSumAggregator extends AbstractAggregator<Double> {
  DoubleSumAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor) {
    super(resource, instrumentationLibraryInfo, descriptor);
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
  public final Double merge(Double a1, Double a2) {
    return a1 + a2;
  }

  @Override
  public MetricData toMetricData(
      Map<Labels, Double> accumulationByLabels, long startEpochNanos, long epochNanos) {
    boolean isMonotonic =
        getInstrumentDescriptor().getType() == InstrumentType.COUNTER
            || getInstrumentDescriptor().getType() == InstrumentType.SUM_OBSERVER;
    return MetricData.createDoubleSum(
        getResource(),
        getInstrumentationLibraryInfo(),
        getInstrumentDescriptor().getName(),
        getInstrumentDescriptor().getDescription(),
        getInstrumentDescriptor().getUnit(),
        DoubleSumData.create(
            isMonotonic,
            AggregationTemporality.CUMULATIVE,
            MetricDataUtils.toDoublePointList(accumulationByLabels, startEpochNanos, epochNanos)));
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
