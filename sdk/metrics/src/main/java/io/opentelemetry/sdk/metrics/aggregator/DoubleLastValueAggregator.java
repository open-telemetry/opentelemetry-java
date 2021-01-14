/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Aggregator that aggregates recorded values by storing the last recorded value.
 *
 * <p>Limitation: The current implementation does not store a time when the value was recorded, so
 * merging multiple LastValueAggregators will not preserve the ordering of records. This is not a
 * problem because LastValueAggregator is currently only available for Observers which record all
 * values once.
 */
@ThreadSafe
final class DoubleLastValueAggregator extends AbstractAggregator<Double> {
  DoubleLastValueAggregator(
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
  public Double merge(Double a1, Double a2) {
    // TODO: Define the order between accumulation.
    return a2;
  }

  @Override
  public MetricData toMetricData(
      Map<Labels, Double> accumulationByLabels, long startEpochNanos, long epochNanos) {
    switch (getInstrumentDescriptor().getType()) {
      case SUM_OBSERVER:
        return MetricData.createDoubleSum(
            getResource(),
            getInstrumentationLibraryInfo(),
            getInstrumentDescriptor().getName(),
            getInstrumentDescriptor().getDescription(),
            getInstrumentDescriptor().getUnit(),
            DoubleSumData.create(
                /* isMonotonic= */ true,
                AggregationTemporality.CUMULATIVE,
                MetricDataUtils.toDoublePointList(
                    accumulationByLabels, startEpochNanos, epochNanos)));
      case UP_DOWN_SUM_OBSERVER:
        return MetricData.createDoubleSum(
            getResource(),
            getInstrumentationLibraryInfo(),
            getInstrumentDescriptor().getName(),
            getInstrumentDescriptor().getDescription(),
            getInstrumentDescriptor().getUnit(),
            DoubleSumData.create(
                /* isMonotonic= */ false,
                AggregationTemporality.CUMULATIVE,
                MetricDataUtils.toDoublePointList(
                    accumulationByLabels, startEpochNanos, epochNanos)));
      case VALUE_OBSERVER:
        return MetricData.createDoubleGauge(
            getResource(),
            getInstrumentationLibraryInfo(),
            getInstrumentDescriptor().getName(),
            getInstrumentDescriptor().getDescription(),
            getInstrumentDescriptor().getUnit(),
            DoubleGaugeData.create(
                MetricDataUtils.toDoublePointList(
                    accumulationByLabels, startEpochNanos, epochNanos)));
      case COUNTER:
      case UP_DOWN_COUNTER:
      case VALUE_RECORDER:
    }
    return null;
  }

  static final class Handle extends AggregatorHandle<Double> {
    @Nullable private static final Double DEFAULT_VALUE = null;
    private final AtomicReference<Double> current = new AtomicReference<>(DEFAULT_VALUE);

    private Handle() {}

    @Override
    protected Double doAccumulateThenReset() {
      return this.current.getAndSet(DEFAULT_VALUE);
    }

    @Override
    protected void doRecordDouble(double value) {
      current.set(value);
    }
  }
}
