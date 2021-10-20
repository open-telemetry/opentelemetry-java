/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataBuilder;
import io.opentelemetry.sdk.metrics.internal.descriptor.MetricDescriptor;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Map;

abstract class AbstractMinMaxSumCountAggregator
    extends AbstractAggregator<MinMaxSumCountAccumulation> {

  AbstractMinMaxSumCountAggregator(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      MetricDescriptor metricDescriptor) {
    super(resource, instrumentationLibraryInfo, metricDescriptor, /* stateful= */ false);
  }

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
  public final MetricData toMetricData(
      Map<Attributes, MinMaxSumCountAccumulation> accumulationByLabels,
      long startEpochNanos,
      long lastCollectionEpoch,
      long epochNanos) {
    return MetricDataBuilder.createDoubleSummary(
        getResource(),
        getInstrumentationLibraryInfo(),
        getMetricDescriptor().getName(),
        getMetricDescriptor().getDescription(),
        getMetricDescriptor().getUnit(),
        DoubleSummaryData.create(
            MetricDataUtils.toDoubleSummaryPointList(
                accumulationByLabels, lastCollectionEpoch, epochNanos)));
  }
}
