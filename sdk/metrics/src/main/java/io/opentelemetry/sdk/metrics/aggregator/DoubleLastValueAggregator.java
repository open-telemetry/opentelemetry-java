/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
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
final class DoubleLastValueAggregator implements Aggregator<Double> {
  private static final DoubleLastValueAggregator INSTANCE = new DoubleLastValueAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  static DoubleLastValueAggregator getInstance() {
    return INSTANCE;
  }

  private DoubleLastValueAggregator() {}

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
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, Double> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<DoublePoint> points =
        MetricDataUtils.toDoublePointList(accumulationByLabels, startEpochNanos, epochNanos);

    switch (descriptor.getType()) {
      case SUM_OBSERVER:
        return MetricDataUtils.toDoubleSumMetricData(
            resource, instrumentationLibraryInfo, descriptor, points, /* isMonotonic= */ true);
      case UP_DOWN_SUM_OBSERVER:
        return MetricDataUtils.toDoubleSumMetricData(
            resource, instrumentationLibraryInfo, descriptor, points, /* isMonotonic= */ false);
      case VALUE_OBSERVER:
        return MetricData.createDoubleGauge(
            resource,
            instrumentationLibraryInfo,
            descriptor.getName(),
            descriptor.getDescription(),
            descriptor.getUnit(),
            DoubleGaugeData.create(points));
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
