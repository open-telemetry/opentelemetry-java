/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.DoubleAdder;

public final class DoubleSumAggregator implements Aggregator<DoubleAccumulation> {
  private static final DoubleSumAggregator INSTANCE = new DoubleSumAggregator();

  /**
   * Returns the instance of this {@link Aggregator}.
   *
   * @return the instance of this {@link Aggregator}.
   */
  public static Aggregator<DoubleAccumulation> getInstance() {
    return INSTANCE;
  }

  private DoubleSumAggregator() {}

  @Override
  public AggregatorHandle<DoubleAccumulation> createHandle() {
    return new Handle();
  }

  @Override
  public DoubleAccumulation accumulateDouble(double value) {
    return DoubleAccumulation.create(value);
  }

  @Override
  public final DoubleAccumulation merge(DoubleAccumulation a1, DoubleAccumulation a2) {
    return DoubleAccumulation.create(a1.getValue() + a2.getValue());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, DoubleAccumulation> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.DoublePoint> points =
        MetricDataUtils.toDoublePointList(accumulationByLabels, startEpochNanos, epochNanos);
    boolean isMonotonic =
        descriptor.getType() == InstrumentType.COUNTER
            || descriptor.getType() == InstrumentType.SUM_OBSERVER;
    return MetricDataUtils.toDoubleSumMetricData(
        resource, instrumentationLibraryInfo, descriptor, points, isMonotonic);
  }

  static final class Handle extends AggregatorHandle<DoubleAccumulation> {
    private final DoubleAdder current = new DoubleAdder();

    @Override
    protected DoubleAccumulation doAccumulateThenReset() {
      return DoubleAccumulation.create(this.current.sumThenReset());
    }

    @Override
    protected void doRecordDouble(double value) {
      current.add(value);
    }
  }
}
