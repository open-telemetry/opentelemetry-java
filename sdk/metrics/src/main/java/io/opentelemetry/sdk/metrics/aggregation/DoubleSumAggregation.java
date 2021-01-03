/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.DoubleAccumulation;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class DoubleSumAggregation extends AbstractAggregation<DoubleAccumulation> {
  static final DoubleSumAggregation INSTANCE =
      new DoubleSumAggregation(DoubleSumAggregator.getInstance());

  private DoubleSumAggregation(Aggregator<DoubleAccumulation> aggregator) {
    super(aggregator);
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

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
