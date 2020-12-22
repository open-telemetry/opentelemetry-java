/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.CountAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class CountAggregation implements Aggregation {
  static final CountAggregation INSTANCE = new CountAggregation();

  private CountAggregation() {}

  @Override
  public AggregatorFactory getAggregatorFactory() {
    return CountAggregator.getFactory();
  }

  @Override
  public Accumulation merge(Accumulation a1, Accumulation a2) {
    LongAccumulation longAccumulation1 = (LongAccumulation) a1;
    LongAccumulation longAccumulation2 = (LongAccumulation) a2;
    return LongAccumulation.create(longAccumulation1.getValue() + longAccumulation2.getValue());
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

    return MetricData.createLongSum(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        "1",
        MetricData.LongSumData.create(
            /* isMonotonic= */ true, MetricData.AggregationTemporality.CUMULATIVE, points));
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
