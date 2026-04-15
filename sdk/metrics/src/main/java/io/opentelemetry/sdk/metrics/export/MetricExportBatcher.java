/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

import io.opentelemetry.sdk.metrics.data.Data;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableExponentialHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableGaugeData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableHistogramData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableMetricData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSumData;
import io.opentelemetry.sdk.metrics.internal.data.ImmutableSummaryData;
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

    // Fill active batch and split overlapping metric points if needed
    for (MetricData metricData : metrics) {
      MetricDataSplitOperationResult splitResult = prepareExportBatches(metricData, currentBatch);
      preparedBatchesForExport.addAll(splitResult.getPreparedBatches());
      currentBatch = splitResult.getLastInProgressBatch();
    }

    // Push trailing capacity block
    if (!currentBatch.isEmpty()) {
      preparedBatchesForExport.add(currentBatch);
    }
    return Collections.unmodifiableCollection(preparedBatchesForExport);
  }

  /**
   * Prepares export batches from a single metric data object. This function only operates on a
   * single metric data object, fills up the current batch with as many points as possible from the
   * metric data object, and then creates new metric data objects for the remaining points.
   *
   * @param metricData The metric data object to split.
   * @param currentBatch The current batch of metric data objects.
   * @return A result containing the prepared batches and the last in-progress batch.
   */
  private MetricDataSplitOperationResult prepareExportBatches(
      MetricData metricData, Collection<MetricData> currentBatch) {
    int currentBatchPoints = 0;
    for (MetricData m : currentBatch) {
      currentBatchPoints += m.getData().getPoints().size();
    }
    int remainingCapacityInCurrentBatch = maxExportBatchSize - currentBatchPoints;
    int totalPointsInMetricData = metricData.getData().getPoints().size();

    if (remainingCapacityInCurrentBatch >= totalPointsInMetricData) {
      currentBatch.add(metricData);
      return new MetricDataSplitOperationResult(Collections.emptyList(), currentBatch);
    } else {
      // Remaining capacity can't hold all points, partition existing metric data object
      List<PointData> originalPointsList = new ArrayList<>(metricData.getData().getPoints());
      Collection<Collection<MetricData>> preparedBatches = new ArrayList<>();

      // Fill current batch buffer completely
      int pointsToTake = remainingCapacityInCurrentBatch;
      int currentIndex = 0;

      if (pointsToTake > 0) {
        currentBatch.add(
            copyMetricData(metricData, originalPointsList, currentIndex, pointsToTake));
        currentIndex = pointsToTake;
        preparedBatches.add(currentBatch);
      }

      // Buffer spillover onto fresh partitions
      int remainingPoints = totalPointsInMetricData - currentIndex;
      currentBatch = new ArrayList<>(maxExportBatchSize);
      remainingCapacityInCurrentBatch = maxExportBatchSize;

      // Iterate extra chunks sized to exact transport constraints
      while (currentIndex < totalPointsInMetricData && remainingPoints > 0) {
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
      case EXPONENTIAL_HISTOGRAM:
        ExponentialHistogramData expHistogramData = original.getExponentialHistogramData();
        return ImmutableMetricData.createExponentialHistogram(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableExponentialHistogramData.create(
                expHistogramData.getAggregationTemporality(),
                (Collection<ExponentialHistogramPointData>) (Collection<?>) points));
      case SUMMARY:
        return ImmutableMetricData.createDoubleSummary(
            original.getResource(),
            original.getInstrumentationScopeInfo(),
            original.getName(),
            original.getDescription(),
            original.getUnit(),
            ImmutableSummaryData.create((Collection<SummaryPointData>) (Collection<?>) points));
    }
    throw new UnsupportedOperationException("Unsupported metric type: " + original.getType());
  }

  /**
   * A data class to store the result of a split operation performed on a single {@link MetricData}
   * object.
   */
  private static class MetricDataSplitOperationResult {
    private final Collection<Collection<MetricData>> preparedBatches;
    private final Collection<MetricData> lastInProgressBatch;

    /**
     * Creates a new MetricDataSplitOperationResult.
     *
     * @param preparedBatches The collection of prepared batches of metric data for export. Each
     *     batch of {@link MetricData} objects is guaranteed to have at most {@link
     *     #maxExportBatchSize} points.
     * @param lastInProgressBatch The last batch that is still in progress. This batch may have less
     *     than {@link #maxExportBatchSize} points.
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
