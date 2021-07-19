/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MetricDataUtils {
  private MetricDataUtils() {}

  static List<LongPointData> toLongPointList(
      Map<Attributes, LongAccumulation> accumulationMap, long startEpochNanos, long epochNanos) {
    List<LongPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (attributes, accumulation) ->
            points.add(
                LongPointData.create(
                    startEpochNanos,
                    epochNanos,
                    attributes,
                    accumulation.getValue(),
                    accumulation.getExemplars())));
    return points;
  }

  static List<DoublePointData> toDoublePointList(
      Map<Attributes, DoubleAccumulation> accumulationMap, long startEpochNanos, long epochNanos) {
    List<DoublePointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (attributes, accumulation) ->
            points.add(
                DoublePointData.create(
                    startEpochNanos,
                    epochNanos,
                    attributes,
                    accumulation.getValue(),
                    accumulation.getExemplars())));
    return points;
  }

  static List<DoubleHistogramPointData> toDoubleHistogramPointList(
      Map<Attributes, HistogramAccumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos,
      List<Double> boundaries) {
    List<DoubleHistogramPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (attributes, aggregator) -> {
          List<Long> counts = new ArrayList<>(aggregator.getCounts().length);
          for (long v : aggregator.getCounts()) {
            counts.add(v);
          }
          points.add(
              DoubleHistogramPointData.create(
                  startEpochNanos,
                  epochNanos,
                  attributes,
                  aggregator.getSum(),
                  boundaries,
                  counts,
                  aggregator.getExemplars()));
        });
    return points;
  }
}
