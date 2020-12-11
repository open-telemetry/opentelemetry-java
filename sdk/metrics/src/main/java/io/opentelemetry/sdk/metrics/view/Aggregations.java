/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.view;

import io.opentelemetry.api.common.Labels;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.aggregator.Aggregator;
import io.opentelemetry.sdk.metrics.aggregator.AggregatorFactory;
import io.opentelemetry.sdk.metrics.aggregator.DoubleLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.DoubleMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.DoubleSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongLastValueAggregator;
import io.opentelemetry.sdk.metrics.aggregator.LongMinMaxSumCount;
import io.opentelemetry.sdk.metrics.aggregator.LongSumAggregator;
import io.opentelemetry.sdk.metrics.aggregator.NoopAggregator;
import io.opentelemetry.sdk.metrics.common.InstrumentDescriptor;
import io.opentelemetry.sdk.metrics.common.InstrumentType;
import io.opentelemetry.sdk.metrics.common.InstrumentValueType;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

public class Aggregations {

  /**
   * Returns an {@code Aggregation} that calculates sum of recorded measurements.
   *
   * @return an {@code Aggregation} that calculates sum of recorded measurements.
   */
  public static Aggregation sum() {
    return Sum.INSTANCE;
  }

  /**
   * Returns an {@code Aggregation} that calculates count of recorded measurements (the number of
   * recorded measurements).
   *
   * @return an {@code Aggregation} that calculates count of recorded measurements (the number of
   *     recorded * measurements).
   */
  public static Aggregation count() {
    return Count.INSTANCE;
  }

  /**
   * Returns an {@code Aggregation} that calculates the last value of all recorded measurements.
   *
   * @return an {@code Aggregation} that calculates the last value of all recorded measurements.
   */
  public static Aggregation lastValue() {
    return LastValue.INSTANCE;
  }

  /**
   * Returns an {@code Aggregation} that calculates a simple summary of all recorded measurements.
   * The summary consists of the count of measurements, the sum of all measurements, the maximum
   * value recorded and the minimum value recorded.
   *
   * @return an {@code Aggregation} that calculates a simple summary of all recorded measurements.
   */
  public static Aggregation minMaxSumCount() {
    return MinMaxSumCount.INSTANCE;
  }

  private enum MinMaxSumCount implements Aggregation {
    INSTANCE;

    @Override
    public AggregatorFactory getAggregatorFactory(InstrumentValueType instrumentValueType) {
      return instrumentValueType == InstrumentValueType.LONG
          ? LongMinMaxSumCount.getFactory()
          : DoubleMinMaxSumCount.getFactory();
    }

    @Override
    public MetricData toMetricData(
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        InstrumentDescriptor descriptor,
        Map<Labels, Aggregator> aggregatorMap,
        long startEpochNanos,
        long epochNanos) {
      if (aggregatorMap.isEmpty()) {
        return null;
      }
      List<MetricData.Point> points = getPointList(aggregatorMap, startEpochNanos, epochNanos);
      return MetricData.createDoubleSummary(
          resource,
          instrumentationLibraryInfo,
          descriptor.getName(),
          descriptor.getDescription(),
          descriptor.getUnit(),
          MetricData.DoubleSummaryData.create(points));
    }

    @Override
    public String toString() {
      return getClass().getSimpleName();
    }
  }

  @Immutable
  private enum Sum implements Aggregation {
    INSTANCE;

    @Override
    public AggregatorFactory getAggregatorFactory(InstrumentValueType instrumentValueType) {
      return instrumentValueType == InstrumentValueType.LONG
          ? LongSumAggregator.getFactory()
          : DoubleSumAggregator.getFactory();
    }

    @Override
    public MetricData toMetricData(
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        InstrumentDescriptor descriptor,
        Map<Labels, Aggregator> aggregatorMap,
        long startEpochNanos,
        long epochNanos) {
      List<MetricData.Point> points = getPointList(aggregatorMap, startEpochNanos, epochNanos);
      boolean isMonotonic =
          descriptor.getType() == InstrumentType.COUNTER
              || descriptor.getType() == InstrumentType.SUM_OBSERVER;
      return getSumMetricData(
          resource, instrumentationLibraryInfo, descriptor, points, isMonotonic);
    }

    @Override
    public String toString() {
      return getClass().getSimpleName();
    }
  }

  @Immutable
  private enum Count implements Aggregation {
    INSTANCE;

    @Override
    public AggregatorFactory getAggregatorFactory(InstrumentValueType instrumentValueType) {
      // TODO: Implement count aggregator and use it here.
      return NoopAggregator.getFactory();
    }

    @Override
    public MetricData toMetricData(
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        InstrumentDescriptor descriptor,
        Map<Labels, Aggregator> aggregatorMap,
        long startEpochNanos,
        long epochNanos) {
      List<MetricData.Point> points = getPointList(aggregatorMap, startEpochNanos, epochNanos);

      return MetricData.createLongSum(
          resource,
          instrumentationLibraryInfo,
          descriptor.getName(),
          descriptor.getDescription(),
          "1",
          MetricData.LongSumData.create(
              /* isMonotonic= */ true, MetricData.AggregationTemporality.CUMULATIVE, points));
    }

    @Override
    public String toString() {
      return getClass().getSimpleName();
    }
  }

  @Immutable
  private enum LastValue implements Aggregation {
    INSTANCE;

    @Override
    public AggregatorFactory getAggregatorFactory(InstrumentValueType instrumentValueType) {
      return instrumentValueType == InstrumentValueType.LONG
          ? LongLastValueAggregator.getFactory()
          : DoubleLastValueAggregator.getFactory();
    }

    @Override
    public MetricData toMetricData(
        Resource resource,
        InstrumentationLibraryInfo instrumentationLibraryInfo,
        InstrumentDescriptor descriptor,
        Map<Labels, Aggregator> aggregatorMap,
        long startEpochNanos,
        long epochNanos) {
      List<MetricData.Point> points = getPointList(aggregatorMap, startEpochNanos, epochNanos);

      switch (descriptor.getType()) {
        case SUM_OBSERVER:
          return getSumMetricData(
              resource, instrumentationLibraryInfo, descriptor, points, /* isMonotonic= */ true);
        case UP_DOWN_SUM_OBSERVER:
          return getSumMetricData(
              resource, instrumentationLibraryInfo, descriptor, points, /* isMonotonic= */ false);
        case VALUE_OBSERVER:
          return getGaugeMetricData(resource, instrumentationLibraryInfo, descriptor, points);
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

  @Nullable
  private static MetricData getSumMetricData(
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
  private static MetricData getGaugeMetricData(
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

  private static List<MetricData.Point> getPointList(
      Map<Labels, Aggregator> aggregatorMap, long startEpochNanos, long epochNanos) {
    List<MetricData.Point> points = new ArrayList<>(aggregatorMap.size());
    aggregatorMap.forEach(
        (labels, aggregator) -> {
          MetricData.Point point = aggregator.toPoint(startEpochNanos, epochNanos, labels);
          if (point == null) {
            return;
          }
          points.add(point);
        });
    return points;
  }

  private Aggregations() {}
}
