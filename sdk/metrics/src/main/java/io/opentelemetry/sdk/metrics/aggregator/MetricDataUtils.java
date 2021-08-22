/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MetricDataUtils {
  private MetricDataUtils() {}

  static List<LongPointData> toLongPointList(
      Map<Attributes, Long> accumulationMap, long startEpochNanos, long epochNanos) {
    List<LongPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, accumulation) ->
            points.add(LongPointData.create(startEpochNanos, epochNanos, labels, accumulation)));
    return points;
  }

  static List<DoublePointData> toDoublePointList(
      Map<Attributes, Double> accumulationMap, long startEpochNanos, long epochNanos) {
    List<DoublePointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, accumulation) ->
            points.add(DoublePointData.create(startEpochNanos, epochNanos, labels, accumulation)));
    return points;
  }

  static List<DoubleSummaryPointData> toDoubleSummaryPointList(
      Map<Attributes, MinMaxSumCountAccumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos) {
    List<DoubleSummaryPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, aggregator) ->
            points.add(aggregator.toPoint(startEpochNanos, epochNanos, labels)));
    return points;
  }

  static List<DoubleHistogramPointData> toDoubleHistogramPointList(
      Map<Attributes, HistogramAccumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos,
      List<Double> boundaries) {
    List<DoubleHistogramPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, aggregator) -> {
          List<Long> counts = new ArrayList<>(aggregator.getCounts().length);
          for (long v : aggregator.getCounts()) {
            counts.add(v);
          }
          points.add(
              DoubleHistogramPointData.create(
                  startEpochNanos, epochNanos, labels, aggregator.getSum(), boundaries, counts));
        });
    return points;
  }
}
