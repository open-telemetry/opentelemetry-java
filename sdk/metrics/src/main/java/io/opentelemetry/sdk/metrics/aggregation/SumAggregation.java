/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.List;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

@Immutable
abstract class SumAggregation<T extends Accumulation> extends AbstractAggregation<T> {
  static final SumAggregation<LongAccumulation> LONG_INSTANCE =
      new SumAggregation<LongAccumulation>(Aggregator.longSum()) {
        @Override
        public LongAccumulation merge(LongAccumulation a1, LongAccumulation a2) {
          return LongAccumulation.create(a1.getValue() + a2.getValue());
        }
      };
  static final SumAggregation<DoubleAccumulation> DOUBLE_INSTANCE =
      new SumAggregation<DoubleAccumulation>(Aggregator.doubleSum()) {
        @Override
        public final DoubleAccumulation merge(DoubleAccumulation a1, DoubleAccumulation a2) {
          return DoubleAccumulation.create(a1.getValue() + a2.getValue());
        }
      };

  private SumAggregation(Aggregator<T> aggregator) {
    super(aggregator);
  }

  @Override
  public MetricData toMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      Map<Labels, T> accumulationByLabels,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.Point> points =
        MetricDataUtils.getPointList(accumulationByLabels, startEpochNanos, epochNanos);
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
