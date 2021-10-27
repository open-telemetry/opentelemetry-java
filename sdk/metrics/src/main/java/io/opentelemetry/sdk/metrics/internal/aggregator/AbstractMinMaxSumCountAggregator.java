/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;

abstract class AbstractMinMaxSumCountAggregator implements Aggregator<MinMaxSumCountAccumulation> {

  AbstractMinMaxSumCountAggregator() {}

  @Override
  public final MinMaxSumCountAccumulation merge(
      MinMaxSumCountAccumulation previous, MinMaxSumCountAccumulation current) {
    return MinMaxSumCountAccumulation.create(
        previous.getCount() + current.getCount(),
        previous.getSum() + current.getSum(),
        Math.min(previous.getMin(), current.getMin()),
        Math.max(previous.getMax(), current.getMax()));
  }

  @Override
  public final MinMaxSumCountAccumulation diff(
      MinMaxSumCountAccumulation previousCumulative, MinMaxSumCountAccumulation currentCumulative) {
    // Summary does not support CUMULATIVE vs. DELTA.
    return currentCumulative;
  }

  @Override
  public final MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibrary,
      MetricDescriptor metricDescriptor,
      Map<Attributes, MinMaxSumCountAccumulation> accumulationByLabels,
      AggregationTemporality temporality,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    // We always report as "summary" temporality.
    return MetricData.createDoubleSummary(
        resource,
        instrumentationLibrary,
        metricDescriptor.getName(),
        metricDescriptor.getDescription(),
        metricDescriptor.getUnit(),
        DoubleSummaryData.create(
            MetricDataUtils.toDoubleSummaryPointList(
                accumulationByLabels, lastCollectionEpoch, epochNanos)));
  }
}
