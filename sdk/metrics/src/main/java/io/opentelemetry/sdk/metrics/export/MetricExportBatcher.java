/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Batches metric data into multiple batches based on the maximum export batch size. This is used by
 * the {@link PeriodicMetricReader} to batch metric data before exporting it.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
class MetricExportBatcher {
  private final int maxExportBatchSize;

  /**
   * Creates a new {@link MetricExportBatcher} with the given maximum export batch size.
   *
   * @param maxExportBatchSize The maximum number of {@link Data#getPoints()} in each export.
   */
  MetricExportBatcher(int maxExportBatchSize) {
    if (maxExportBatchSize <= 0) {
      throw new IllegalArgumentException("maxExportBatchSize must be positive");
    }
    this.maxExportBatchSize = maxExportBatchSize;
  }

  @Override
  public String toString() {
    return "MetricExportBatcher{maxExportBatchSize=" + maxExportBatchSize + "}";
  }

  /**
   * Batches the given metric data into multiple batches based on the maximum export batch size.
   *
   * @param metrics The collection of metric data objects to batch based on the number of data
   *     points they contain.
   * @return A collection of batches of metric data.
   */
  Collection<Collection<MetricData>> batchMetrics(Collection<MetricData> metrics) {
    if (metrics.isEmpty()) {
      return Collections.emptyList();
    }

    Collection<Collection<MetricData>> batches = new ArrayList<>();
    int currentBatchRemainingCapacity = maxExportBatchSize;

    for (MetricData metricData : metrics) {
      MetricDataSplitOperationResult splitResult =
          splitMetricData(metricData, currentBatchRemainingCapacity);
      batches.add(splitResult.getBatchedMetricData());
      currentBatchRemainingCapacity = splitResult.getLastBatchRemainingCapacity();
    }

    return Collections.unmodifiableCollection(batches);
  }

  /**
   * Splits a MetricData object into multiple MetricData objects if the number of points exceeds the
   * remaining capacity in the current batch. This function tries to fill the current batch with as
   * many points as possible from the given metric data.
   *
   * <p>If the number of points in the metric data is less than or equal to the remaining capacity
   * in the current batch, it will return a single MetricData object with all the points.
   *
   * <p>If the number of points in the metric data is greater than the remaining capacity in the
   * current batch, it will return multiple MetricData objects, each with a subset of the points
   * from the original metric data.
   *
   * @param metricData The MetricData object to split.
   * @param remainingCapacityInCurrentBatch The remaining capacity in the current batch being used.
   * @return A MetricDataSplitOperationResult containing the batched metric data and the remaining
   *     capacity in the last batch.
   */
  private MetricDataSplitOperationResult splitMetricData(
      MetricData metricData, int remainingCapacityInCurrentBatch) {
    int totalPointsInMetricData = metricData.getData().getPoints().size();
    if (remainingCapacityInCurrentBatch >= totalPointsInMetricData) {
      // We have enough capacity in the current batch to fit all points in this
      // MetricData
      return new MetricDataSplitOperationResult(
          Collections.singleton(metricData),
          remainingCapacityInCurrentBatch - totalPointsInMetricData);
    } else {
      // We don't have enough capacity in the current batch. Split this MetricData
      // into multiple MetricData objects.
      Collection<MetricData> splittedMetrics = new ArrayList<>();
      // List of all points in the metric data - to avoid creating a new one in each
      // call to copyMetricData
      List<PointData> originalPointsList = new ArrayList<>(metricData.getData().getPoints());

      // Split the points into chunks of size maxExportBatchSize
      // From the first chunk, take as many points as possible to fill current batch
      int pointsToTake = remainingCapacityInCurrentBatch;
      int currentIndex = 0;

      if (pointsToTake > 0) {
        splittedMetrics.add(
            copyMetricData(metricData, originalPointsList, currentIndex, pointsToTake));
        currentIndex = pointsToTake;
        remainingCapacityInCurrentBatch -= pointsToTake; // should be 0
      }

      int remainingPoints = totalPointsInMetricData - currentIndex;
      // Add remaining points in chunks of size maxExportBatchSize
      while (currentIndex < totalPointsInMetricData) {
        pointsToTake = Math.min(remainingPoints, maxExportBatchSize);
        splittedMetrics.add(
            copyMetricData(metricData, originalPointsList, currentIndex, pointsToTake));
        currentIndex += pointsToTake;
        remainingPoints -= pointsToTake;
      }

      int lastBatchRemainingCapacity = maxExportBatchSize - pointsToTake;
      return new MetricDataSplitOperationResult(splittedMetrics, lastBatchRemainingCapacity);
    }
  }

  private static MetricData copyMetricData(
      MetricData original,
      List<PointData> originalPointsList,
      int dataPointsOffset,
      int dataPointsToTake) {
    List<PointData> points =
        originalPointsList.subList(dataPointsOffset, dataPointsOffset + dataPointsToTake);
    return createMetricDataWithPoints(original, points);
  }

  /**
   * Creates a new MetricData with the given points.
   *
   * @param original The original MetricData.
   * @param points The points to use for the new MetricData.
   * @return A new MetricData with the given points.
   */
  @SuppressWarnings("unchecked")
  private static MetricData createMetricDataWithPoints(
      MetricData original, Collection<PointData> points) {
    switch (original.getType()) {
      case DOUBLE_GAUGE:
        return ImmutableMetricData.createDoubleGauge(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableGaugeData.create((Collection<DoublePointData>) (Collection<?>) points));
      case LONG_GAUGE:
        return ImmutableMetricData.createLongGauge(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableGaugeData.create((Collection<LongPointData>) (Collection<?>) points));
      case DOUBLE_SUM:
        SumData<DoublePointData> doubleSumData = original.getDoubleSumData();
        return ImmutableMetricData.createDoubleSum(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableSumData.create(
                doubleSumData.isMonotonic(),
                doubleSumData.getAggregationTemporality(),
                (Collection<DoublePointData>) (Collection<?>) points));
      case LONG_SUM:
        SumData<LongPointData> longSumData = original.getLongSumData();
        return ImmutableMetricData.createLongSum(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableSumData.create(
                longSumData.isMonotonic(),
                longSumData.getAggregationTemporality(),
                (Collection<LongPointData>) (Collection<?>) points));
      case HISTOGRAM:
        HistogramData histogramData = original.getHistogramData();
        return ImmutableMetricData.createDoubleHistogram(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableHistogramData.create(
                histogramData.getAggregationTemporality(),
                (Collection<HistogramPointData>) (Collection<?>) points));
      default:
        throw new UnsupportedOperationException("Unsupported metric type: " + original.getType());
    }
  }

  /** A result of a metric data split operation. */
  private static class MetricDataSplitOperationResult {
    private final Collection<MetricData> batchedMetricData;
    private final int lastBatchRemainingCapacity;

    /**
     * Creates a new MetricDataSplitOperationResult.
     *
     * @param batchedMetricData The collection of batched metric data.
     * @param lastBatchRemainingCapacity The remaining capacity in the last batch.
     */
    MetricDataSplitOperationResult(
        Collection<MetricData> batchedMetricData, int lastBatchRemainingCapacity) {
      this.batchedMetricData = batchedMetricData;
      this.lastBatchRemainingCapacity = lastBatchRemainingCapacity;
    }

    Collection<MetricData> getBatchedMetricData() {
      return batchedMetricData;
    }

    int getLastBatchRemainingCapacity() {
      return lastBatchRemainingCapacity;
    }
  }
}
