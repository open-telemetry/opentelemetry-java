/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.MinMaxSumCountAccumulation;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCountAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCountAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class MinMaxSumCountAggregation extends AbstractAggregation<MinMaxSumCountAccumulation> {
  static final MinMaxSumCountAggregation LONG_INSTANCE =
      new MinMaxSumCountAggregation(LongMinMaxSumCountAggregator.getInstance());
  static final MinMaxSumCountAggregation DOUBLE_INSTANCE =
      new MinMaxSumCountAggregation(DoubleMinMaxSumCountAggregator.getInstance());

  private MinMaxSumCountAggregation(Aggregator<MinMaxSumCountAccumulation> aggregator) {
    super(aggregator);
  }

  @Override
  public MinMaxSumCountAccumulation merge(
      MinMaxSumCountAccumulation a1, MinMaxSumCountAccumulation a2) {
    return MinMaxSumCountAccumulation.create(
        a1.getCount() + a2.getCount(),
        a1.getSum() + a2.getSum(),
        Math.min(a1.getMin(), a2.getMin()),
        Math.max(a1.getMax(), a2.getMax()));
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, MinMaxSumCountAccumulation> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.Point> points =
        MetricDataUtils.getPointList(accumulationByLabels, startEpochNanos, epochNanos);
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
