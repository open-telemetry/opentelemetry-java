/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCountAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCountAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class MinMaxSumCountAggregation implements Aggregation {
  static final MinMaxSumCountAggregation LONG_INSTANCE =
      new MinMaxSumCountAggregation(LongMinMaxSumCountAggregator.getFactory());
  static final MinMaxSumCountAggregation DOUBLE_INSTANCE =
      new MinMaxSumCountAggregation(DoubleMinMaxSumCountAggregator.getFactory());

  private final AggregatorFactory aggregatorFactory;

  private MinMaxSumCountAggregation(AggregatorFactory aggregatorFactory) {
    this.aggregatorFactory = aggregatorFactory;
  }

  @Override
  public AggregatorFactory getAggregatorFactory() {
    return aggregatorFactory;
  }

  @Override
  public Accumulation merge(Accumulation a1, Accumulation a2) {
    MinMaxSumCountAccumulation minMaxSumCountAccumulation1 = (MinMaxSumCountAccumulation) a1;
    MinMaxSumCountAccumulation minMaxSumCountAccumulation2 = (MinMaxSumCountAccumulation) a2;
    return MinMaxSumCountAccumulation.create(
        minMaxSumCountAccumulation1.getCount() + minMaxSumCountAccumulation2.getCount(),
        minMaxSumCountAccumulation1.getSum() + minMaxSumCountAccumulation2.getSum(),
        Math.min(minMaxSumCountAccumulation1.getMin(), minMaxSumCountAccumulation2.getMin()),
        Math.max(minMaxSumCountAccumulation1.getMax(), minMaxSumCountAccumulation2.getMax()));
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, Accumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos) {
    if (accumulationMap.isEmpty()) {
      return null;
    }
    List<MetricData.Point> points =
        MetricDataUtils.getPointList(accumulationMap, startEpochNanos, epochNanos);
    return MetricData.createDoubleSummary(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        MetricData.DoubleSummaryData.create(points));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
