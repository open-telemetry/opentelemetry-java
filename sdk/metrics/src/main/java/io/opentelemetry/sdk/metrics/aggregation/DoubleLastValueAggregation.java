/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class DoubleLastValueAggregation extends AbstractAggregation<DoubleAccumulation> {
  static final DoubleLastValueAggregation INSTANCE =
      new DoubleLastValueAggregation(DoubleLastValueAggregator.getInstance());

  private DoubleLastValueAggregation(Aggregator<DoubleAccumulation> aggregator) {
    super(aggregator);
  }

  @Override
  public DoubleAccumulation merge(DoubleAccumulation a1, DoubleAccumulation a2) {
    // TODO: Define the order between accumulation.
    return a2;
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
            MetricData.DoubleGaugeData.create(points));
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
