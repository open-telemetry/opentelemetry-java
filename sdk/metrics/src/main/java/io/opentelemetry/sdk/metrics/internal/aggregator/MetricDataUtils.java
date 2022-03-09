/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.aggregator;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableDoublePointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableLongPointData;
import io.opentelemetry.sdk.metrics.internal.data.exponentialhistogram.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.internal.descriptor.InstrumentDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class MetricDataUtils {
  private MetricDataUtils() {}

  /** Returns true if the instrument does not allow negative measurements. */
  @SuppressWarnings("deprecation") // Support OBSERVABLE_SUM until removed
  static boolean isMonotonicInstrument(InstrumentDescriptor descriptor) {
    InstrumentType type = descriptor.getType();
    return type == InstrumentType.HISTOGRAM
        || type == InstrumentType.COUNTER
        || type == InstrumentType.OBSERVABLE_COUNTER;
  }

  static List<LongPointData> toLongPointList(
      Map<Attributes, LongAccumulation> accumulationMap, long startEpochNanos, long epochNanos) {
    List<LongPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, accumulation) ->
            points.add(
                ImmutableLongPointData.create(
                    startEpochNanos,
                    epochNanos,
                    labels,
                    accumulation.getValue(),
                    accumulation.getExemplars())));
    return points;
  }

  static List<DoublePointData> toDoublePointList(
      Map<Attributes, DoubleAccumulation> accumulationMap, long startEpochNanos, long epochNanos) {
    List<DoublePointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, accumulation) ->
            points.add(
                ImmutableDoublePointData.create(
                    startEpochNanos,
                    epochNanos,
                    labels,
                    accumulation.getValue(),
                    accumulation.getExemplars())));
    return points;
  }

  static List<HistogramPointData> toDoubleHistogramPointList(
      Map<Attributes, HistogramAccumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos,
      List<Double> boundaries) {
    List<HistogramPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (labels, aggregator) -> {
          List<Long> counts = PrimitiveLongList.wrap(aggregator.getCounts().clone());
          points.add(
              ImmutableHistogramPointData.create(
                  startEpochNanos,
                  epochNanos,
                  labels,
                  aggregator.getSum(),
                  boundaries,
                  counts,
                  aggregator.getExemplars()));
        });
    return points;
  }

  static List<ExponentialHistogramPointData> toExponentialHistogramPointList(
      Map<Attributes, ExponentialHistogramAccumulation> accumulationMap,
      long startEpochNanos,
      long epochNanos) {
    List<ExponentialHistogramPointData> points = new ArrayList<>(accumulationMap.size());
    accumulationMap.forEach(
        (attributes, aggregator) ->
            points.add(
                ExponentialHistogramPointData.create(
                    aggregator.getScale(),
                    aggregator.getSum(),
                    aggregator.getZeroCount(),
                    aggregator.getPositiveBuckets(),
                    aggregator.getNegativeBuckets(),
                    startEpochNanos,
                    epochNanos,
                    attributes,
                    aggregator.getExemplars())));
    return points;
  }
}
