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

    Collection<Collection<MetricData>> preparedBatchesForExport = new ArrayList<>();
    Collection<MetricData> currentBatch = new ArrayList<>(maxExportBatchSize);

    // Iterate through each MetricData and fill up the current batch, splitting if
    // necessary
    for (MetricData metricData : metrics) {
      MetricDataSplitOperationResult splitResult = prepareExportBatches(metricData, currentBatch);
      preparedBatchesForExport.addAll(splitResult.getPreparedBatches());
      currentBatch = splitResult.getLastInProgressBatch();
    }

    // Add the last in-progress batch if it is not empty
    if (!currentBatch.isEmpty()) {
      preparedBatchesForExport.add(currentBatch);
    }

    return Collections.unmodifiableCollection(preparedBatchesForExport);
  }

  /**
   * Prepares export batches from a single metric data object. This function only
   * operates on a
   * single metric data object, fills up the current batch with as many points as
   * possible from the
   * metric data object, and then creates new metric data objects for the
   * remaining points.
   *
   * @param metricData   The metric data object to split.
   * @param currentBatch The current batch of metric data objects.
   * @return A result containing the prepared batches and the last in-progress
   *         batch.
   */
  private MetricDataSplitOperationResult prepareExportBatches(
      MetricData metricData, Collection<MetricData> currentBatch) {
    int remainingCapacityInCurrentBatch = maxExportBatchSize - currentBatch.size();
    int totalPointsInMetricData = metricData.getData().getPoints().size();

    if (remainingCapacityInCurrentBatch >= totalPointsInMetricData) {
      currentBatch.add(metricData);
      return new MetricDataSplitOperationResult(Collections.emptyList(), currentBatch);
    } else {
      // remaining capacity in current batch cannot hold all points from metric data
      // split the metric data into multiple metric data objects
      List<PointData> originalPointsList = new ArrayList<>(metricData.getData().getPoints());
      Collection<Collection<MetricData>> preparedBatches = new ArrayList<>();

      // Split the points into chunks of size maxExportBatchSize
      // From the first chunk, take as many points as possible to fill current batch
      int pointsToTake = remainingCapacityInCurrentBatch;
      int currentIndex = 0;

      // fill the current batch and add it to prepared batches
      if (pointsToTake > 0) {
        currentBatch.add(
            copyMetricData(metricData, originalPointsList, currentIndex, pointsToTake));
        currentIndex = pointsToTake;
        preparedBatches.add(currentBatch);
      }

      // If the current metric contains more data points than could fit into the
      // filled batch above,
      // we initialize a fresh batch to receive the spillover points on subsequent
      // iterations.
      int remainingPoints = totalPointsInMetricData - currentIndex;
      currentBatch = new ArrayList<>(maxExportBatchSize);
      remainingCapacityInCurrentBatch = maxExportBatchSize;

      // Add remaining points in chunks of size maxExportBatchSize
      while (currentIndex < totalPointsInMetricData && remainingPoints > 0) {
        // There are still more points in the current metricData
        // Take as many points as possible to fill current batch up till remaining
        // capacity
        pointsToTake = Math.min(remainingPoints, remainingCapacityInCurrentBatch);
        currentBatch.add(
            copyMetricData(metricData, originalPointsList, currentIndex, pointsToTake));
        currentIndex += pointsToTake;
        remainingPoints -= pointsToTake;
        remainingCapacityInCurrentBatch -= pointsToTake;
        if (remainingCapacityInCurrentBatch == 0) {
          preparedBatches.add(currentBatch);
          currentBatch = new ArrayList<>(maxExportBatchSize);
          remainingCapacityInCurrentBatch = maxExportBatchSize;
        }
      }
      return new MetricDataSplitOperationResult(preparedBatches, currentBatch);
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

  /**
   * A data class to store the result of a split operation performed on a single
   * {@link MetricData}
   * object.
   */
  private static class MetricDataSplitOperationResult {
    private final Collection<Collection<MetricData>> preparedBatches;
    private final Collection<MetricData> lastInProgressBatch;

    /**
     * Creates a new MetricDataSplitOperationResult.
     *
     * @param preparedBatches     The collection of prepared batches of metric data
     *                            for export. Each
     *                            batch of {@link MetricData} objects is guaranteed
     *                            to have at most {@link
     *                            #maxExportBatchSize} points.
     * @param lastInProgressBatch The last batch that is still in progress. This
     *                            batch may have less
     *                            than {@link #maxExportBatchSize} points.
     */
    MetricDataSplitOperationResult(
        Collection<Collection<MetricData>> preparedBatches,
        Collection<MetricData> lastInProgressBatch) {
      this.preparedBatches = preparedBatches;
      this.lastInProgressBatch = lastInProgressBatch;
    }

    Collection<Collection<MetricData>> getPreparedBatches() {
      return preparedBatches;
    }

    Collection<MetricData> getLastInProgressBatch() {
      return lastInProgressBatch;
    }
  }
}
