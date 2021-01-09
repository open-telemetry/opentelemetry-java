/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MetricDataUtils {
  private MetricDataUtils() {}

  static MetricData toLongSumMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      List<MetricData.LongPoint> points,
      boolean isMonotonic) {
    return MetricData.createLongSum(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        MetricData.LongSumData.create(
            isMonotonic, MetricData.AggregationTemporality.CUMULATIVE, points));
  }

  static MetricData toDoubleSumMetricData(
      Resource resource,
      InstrumentationLibraryInfo instrumentationLibraryInfo,
      InstrumentDescriptor descriptor,
      List<MetricData.DoublePoint> points,
      boolean isMonotonic) {
    return MetricData.createDoubleSum(
        resource,
        instrumentationLibraryInfo,
        descriptor.getName(),
        descriptor.getDescription(),
        descriptor.getUnit(),
        MetricData.DoubleSumData.create(
            isMonotonic, MetricData.AggregationTemporality.CUMULATIVE, points));
  }

  static List<MetricData.LongPoint> toLongPointList(
      Map<Labels, Long> accumulationMap, long startEpochNanos, long epochNanos) {
    List<MetricData.LongPoint> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, accumulation) ->
            points.add(
                MetricData.LongPoint.create(startEpochNanos, epochNanos, labels, accumulation)));
    return points;
  }

  static List<MetricData.DoublePoint> toDoublePointList(
      Map<Labels, Double> accumulationMap, long startEpochNanos, long epochNanos) {
    List<MetricData.DoublePoint> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, accumulation) ->
            points.add(
                MetricData.DoublePoint.create(startEpochNanos, epochNanos, labels, accumulation)));
    return points;
  }

  static List<MetricData.DoubleSummaryPoint> toDoubleSummaryPointList(
      Map<Labels, MinMaxSumCountAccumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos) {
    List<MetricData.DoubleSummaryPoint> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, aggregator) ->
            points.add(aggregator.toPoint(startEpochNanos, epochNanos, labels)));
    return points;
  }
}
