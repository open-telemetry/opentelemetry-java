/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregation;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.accumulation.Accumulation;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

final class MetricDataUtils {
  private MetricDataUtils() {}

  @Nullable
  static MetricData getSumMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      List<MetricData.Point> points,
      boolean isMonotonic) {
    switch (descriptor.getValueType()) {
      case LONG:
        return MetricData.createLongSum(
            resource,
            instrumentationLibraryInfo,
            descriptor.getName(),
            descriptor.getDescription(),
            descriptor.getUnit(),
            MetricData.LongSumData.create(
                isMonotonic, MetricData.AggregationTemporality.CUMULATIVE, points));
      case DOUBLE:
        return MetricData.createDoubleSum(
            resource,
            instrumentationLibraryInfo,
            descriptor.getName(),
            descriptor.getDescription(),
            descriptor.getUnit(),
            MetricData.DoubleSumData.create(
                isMonotonic, MetricData.AggregationTemporality.CUMULATIVE, points));
    }
    return null;
  }

  @Nullable
  static MetricData getGaugeMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      List<MetricData.Point> points) {
    switch (descriptor.getValueType()) {
      case LONG:
        return MetricData.createLongGauge(
            resource,
            instrumentationLibraryInfo,
            descriptor.getName(),
            descriptor.getDescription(),
            descriptor.getUnit(),
            MetricData.LongGaugeData.create(points));
      case DOUBLE:
        return MetricData.createDoubleGauge(
            resource,
            instrumentationLibraryInfo,
            descriptor.getName(),
            descriptor.getDescription(),
            descriptor.getUnit(),
            MetricData.DoubleGaugeData.create(points));
    }
    return null;
  }

  static List<MetricData.Point> getPointList(
      Map<Labels, ? extends Accumulation> accumulationMap, long startEpochNanos, long epochNanos) {
    List<MetricData.Point> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, aggregator) -> {
          MetricData.Point point = aggregator.toPoint(startEpochNanos, epochNanos, labels);
          if (point == null) {
            return;
          }
          points.add(point);
        });
    return points;
  }
}
