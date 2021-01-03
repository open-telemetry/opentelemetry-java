/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.LongAccumulation;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
final class LongSumAggregation extends AbstractAggregation<LongAccumulation> {
  static final LongSumAggregation INSTANCE =
      new LongSumAggregation(LongSumAggregator.getInstance());

  private LongSumAggregation(Aggregator<LongAccumulation> aggregator) {
    super(aggregator);
  }

  @Override
  public LongAccumulation merge(LongAccumulation a1, LongAccumulation a2) {
    return LongAccumulation.create(a1.getValue() + a2.getValue());
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, LongAccumulation> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.LongPoint> points =
        MetricDataUtils.toLongPointList(accumulationByLabels, startEpochNanos, epochNanos);
    boolean isMonotonic =
        descriptor.getType() == InstrumentType.COUNTER
            || descriptor.getType() == InstrumentType.SUM_OBSERVER;
    return MetricDataUtils.toLongSumMetricData(
        resource, instrumentationLibraryInfo, descriptor, points, isMonotonic);
  }

  @Override
  public String toString() {
    return getClass().getSimpleName();
  }
}
