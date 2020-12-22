/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class LastValueAggregation implements Aggregation {
  static final LastValueAggregation LONG_INSTANCE =
      new LastValueAggregation(LongLastValueAggregator.getFactory());
  static final LastValueAggregation DOUBLE_INSTANCE =
      new LastValueAggregation(DoubleLastValueAggregator.getFactory());

  private final AggregatorFactory aggregatorFactory;

  private LastValueAggregation(AggregatorFactory aggregatorFactory) {
    this.aggregatorFactory = aggregatorFactory;
  }

  @Override
  public AggregatorFactory getAggregatorFactory() {
    return aggregatorFactory;
  }

  @Override
  public Accumulation merge(Accumulation a1, Accumulation a2) {
    // TODO: Define the order between accumulation.
    return a2;
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

    switch (descriptor.getType()) {
      case SUM_OBSERVER:
        return MetricDataUtils.getSumMetricData(
            resource, instrumentationLibraryInfo, descriptor, points, /* isMonotonic= */ true);
      case UP_DOWN_SUM_OBSERVER:
        return MetricDataUtils.getSumMetricData(
            resource, instrumentationLibraryInfo, descriptor, points, /* isMonotonic= */ false);
      case VALUE_OBSERVER:
        return MetricDataUtils.getGaugeMetricData(
            resource, instrumentationLibraryInfo, descriptor, points);
      case COUNTER:
      case UP_DOWN_COUNTER:
      case VALUE_RECORDER:
    }
    return null;
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
