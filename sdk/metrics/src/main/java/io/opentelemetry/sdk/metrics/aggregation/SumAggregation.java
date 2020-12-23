/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class SumAggregation implements Aggregation {
  static final SumAggregation LONG_INSTANCE = new SumAggregation(LongSumAggregator.getFactory());
  static final SumAggregation DOUBLE_INSTANCE =
      new SumAggregation(DoubleSumAggregator.getFactory());

  private final AggregatorFactory<?> aggregatorFactory;

  private SumAggregation(AggregatorFactory<?> aggregatorFactory) {
    this.aggregatorFactory = aggregatorFactory;
  }

  @Override
  public AggregatorFactory<?> getAggregatorFactory() {
    return aggregatorFactory;
  }

  @Override
  public Accumulation merge(Accumulation a1, Accumulation a2) {
    // TODO: Fix this by splitting the Aggregation per instrument value type.
    if (a1 instanceof LongAccumulation) {
      LongAccumulation longAccumulation1 = (LongAccumulation) a1;
      LongAccumulation longAccumulation2 = (LongAccumulation) a2;
      return LongAccumulation.create(longAccumulation1.getValue() + longAccumulation2.getValue());
    }
    DoubleAccumulation longAccumulation1 = (DoubleAccumulation) a1;
    DoubleAccumulation longAccumulation2 = (DoubleAccumulation) a2;
    return DoubleAccumulation.create(longAccumulation1.getValue() + longAccumulation2.getValue());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, Accumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.Point> points =
        MetricDataUtils.getPointList(accumulationMap, startEpochNanos, epochNanos);
    boolean isMonotonic =
        descriptor.getType() == InstrumentType.COUNTER
            || descriptor.getType() == InstrumentType.SUM_OBSERVER;
    return MetricDataUtils.getSumMetricData(
        resource, instrumentationLibraryInfo, descriptor, points, isMonotonic);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
