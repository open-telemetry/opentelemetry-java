/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.export;

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

  private MetricExportBatcher() {}

  private static void validateMaxExportBatchSize(int maxExportBatchSize) {
    if (maxExportBatchSize <= 0) {
      throw new IllegalArgumentException("maxExportBatchSize must be positive");
    }
  }

  /**
   * Batches the given metric data into multiple batches based on the maximum export batch size.
   *
   * @param metrics The collection of metric data objects to batch based on the number of data
   *     points they contain.
   * @return A collection of batches of metric data.
   */
  static Collection<Collection<MetricData>> batchMetrics(
      Collection<MetricData> metrics, int maxExportBatchSize) {
    validateMaxExportBatchSize(maxExportBatchSize);
    if (metrics.isEmpty()) {
      return Collections.emptyList();
    }
    Collection<Collection<MetricData>> preparedBatchesForExport = new ArrayList<>();
    List<MetricData> currentBatch = new ArrayList<>(maxExportBatchSize);
    int currentPointsInBatch = 0;
    for (MetricData metricData : metrics) {
      int totalPointsInMetric = metricData.getData().getPoints().size();
      if (currentPointsInBatch + totalPointsInMetric <= maxExportBatchSize) {
        currentBatch.add(metricData);
        currentPointsInBatch += totalPointsInMetric;
        continue;
      }
      int currentIndex = 0;
      List<PointData> originalPointsList = new ArrayList<>(metricData.getData().getPoints());
      while (currentIndex < totalPointsInMetric) {
        if (currentPointsInBatch == maxExportBatchSize) {
          preparedBatchesForExport.add(currentBatch);
          currentBatch = new ArrayList<>(maxExportBatchSize);
          currentPointsInBatch = 0;
        }
        int pointsToTake =
            Math.min(maxExportBatchSize - currentPointsInBatch, totalPointsInMetric - currentIndex);
        currentBatch.add(
            copyMetricData(metricData, originalPointsList, currentIndex, pointsToTake));
        currentPointsInBatch += pointsToTake;
        currentIndex += pointsToTake;
      }
    }
    if (!currentBatch.isEmpty()) {
      preparedBatchesForExport.add(currentBatch);
    }
    return Collections.unmodifiableCollection(preparedBatchesForExport);
  }

  private static MetricData copyMetricData(
      MetricData original,
      List<PointData> originalPointsList,
      int dataPointsOffset,
      int dataPointsToTake) {
    List<PointData> points =
        Collections.unmodifiableList(
            new ArrayList<>(
                originalPointsList.subList(dataPointsOffset, dataPointsOffset + dataPointsToTake)));
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
}
