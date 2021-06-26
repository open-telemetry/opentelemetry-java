/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.prometheus.client.Collector.doubleToGoString;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

/**
 * Util methods to convert OpenTelemetry Metrics data models to Prometheus data models.
 *
 * <p>Each OpenTelemetry {@link MetricData} will be converted to a Prometheus {@link
 * MetricFamilySamples}, and each {@code Point} of the {@link MetricData} will be converted to
 * Prometheus {@link Sample}s.
 *
 * <p>{@code DoublePoint}, {@code LongPoint} will be converted to a single {@link Sample}. {@code
 * Summary} will be converted to two {@link Sample}s (sum and count) plus the number of Percentile
 * values {@code Sample}s
 *
 * <p>Please note that Prometheus Metric and Label name can only have alphanumeric characters and
 * underscore. All other characters will be sanitized by underscores.
 */
final class MetricAdapter {

  static final String SAMPLE_SUFFIX_COUNT = "_count";
  static final String SAMPLE_SUFFIX_SUM = "_sum";
  static final String SAMPLE_SUFFIX_BUCKET = "_bucket";
  static final String LABEL_NAME_QUANTILE = "quantile";
  static final String LABEL_NAME_LE = "le";

  // Converts a MetricData to a Prometheus MetricFamilySamples.
  static MetricFamilySamples toMetricFamilySamples(MetricData metricData) {
    String cleanMetricName = cleanMetricName(metricData.getName());
    Collector.Type type = toMetricFamilyType(metricData);

    return new MetricFamilySamples(
        cleanMetricName,
        type,
        metricData.getDescription(),
        toSamples(cleanMetricName, metricData.getType(), getPoints(metricData)));
  }

  private static String cleanMetricName(String descriptorMetricName) {
    return Collector.sanitizeMetricName(descriptorMetricName);
  }

  static Collector.Type toMetricFamilyType(MetricData metricData) {
    switch (metricData.getType()) {
      case LONG_GAUGE:
      case DOUBLE_GAUGE:
        return Collector.Type.GAUGE;
      case LONG_SUM:
        LongSumData longSumData = metricData.getLongSumData();
        if (longSumData.isMonotonic()
            && longSumData.getAggregationTemporality() == AggregationTemporality.CUMULATIVE) {
          return Collector.Type.COUNTER;
        }
        return Collector.Type.GAUGE;
      case DOUBLE_SUM:
        DoubleSumData doubleSumData = metricData.getDoubleSumData();
        if (doubleSumData.isMonotonic()
            && doubleSumData.getAggregationTemporality() == AggregationTemporality.CUMULATIVE) {
          return Collector.Type.COUNTER;
        }
        return Collector.Type.GAUGE;
      case SUMMARY:
        return Collector.Type.SUMMARY;
      case HISTOGRAM:
        return Collector.Type.HISTOGRAM;
    }
    return Collector.Type.UNKNOWN;
  }

  private static final Function<String, String> sanitizer = new LabelNameSanitizer();

  // Converts a list of points from MetricData to a list of Prometheus Samples.
  static List<Sample> toSamples(
      String name, MetricDataType type, Collection<? extends PointData> points) {
    final List<Sample> samples = new ArrayList<>(estimateNumSamples(points.size(), type));

    for (PointData pointData : points) {
      Attributes attributes = pointData.getAttributes();
      final List<String> labelNames = new ArrayList<>(attributes.size());
      final List<String> labelValues = new ArrayList<>(attributes.size());
      if (attributes.size() != 0) {
        attributes.forEach(
            (key, value) -> {
              String sanitizedLabelName = sanitizer.apply(key.getKey());
              labelNames.add(sanitizedLabelName);
              // TODO: We want to create an error-log if there is overlap in toString of attribute
              // values for the same key name.
              labelValues.add(value == null ? "" : value.toString());
            });
      }

      switch (type) {
        case DOUBLE_SUM:
        case DOUBLE_GAUGE:
          DoublePointData doublePoint = (DoublePointData) pointData;
          samples.add(new Sample(name, labelNames, labelValues, doublePoint.getValue()));
          break;
        case LONG_SUM:
        case LONG_GAUGE:
          LongPointData longPoint = (LongPointData) pointData;
          samples.add(new Sample(name, labelNames, labelValues, longPoint.getValue()));
          break;
        case SUMMARY:
          addSummarySamples(
              (DoubleSummaryPointData) pointData, name, labelNames, labelValues, samples);
          break;
        case HISTOGRAM:
          addHistogramSamples(
              (DoubleHistogramPointData) pointData, name, labelNames, labelValues, samples);
          break;
      }
    }
    return samples;
  }

  private static void addSummarySamples(
      DoubleSummaryPointData doubleSummaryPoint,
      String name,
      List<String> labelNames,
      List<String> labelValues,
      List<Sample> samples) {
    samples.add(
        new Sample(
            name + SAMPLE_SUFFIX_COUNT, labelNames, labelValues, doubleSummaryPoint.getCount()));
    samples.add(
        new Sample(name + SAMPLE_SUFFIX_SUM, labelNames, labelValues, doubleSummaryPoint.getSum()));
    List<ValueAtPercentile> valueAtPercentiles = doubleSummaryPoint.getPercentileValues();
    List<String> labelNamesWithQuantile = new ArrayList<>(labelNames.size());
    labelNamesWithQuantile.addAll(labelNames);
    labelNamesWithQuantile.add(LABEL_NAME_QUANTILE);
    for (ValueAtPercentile valueAtPercentile : valueAtPercentiles) {
      List<String> labelValuesWithQuantile = new ArrayList<>(labelValues.size());
      labelValuesWithQuantile.addAll(labelValues);
      labelValuesWithQuantile.add(doubleToGoString(valueAtPercentile.getPercentile()));
      samples.add(
          new Sample(
              name, labelNamesWithQuantile, labelValuesWithQuantile, valueAtPercentile.getValue()));
    }
  }

  private static void addHistogramSamples(
      DoubleHistogramPointData doubleHistogramPointData,
      String name,
      List<String> labelNames,
      List<String> labelValues,
      List<Sample> samples) {
    samples.add(
        new Sample(
            name + SAMPLE_SUFFIX_COUNT,
            labelNames,
            labelValues,
            doubleHistogramPointData.getCount()));
    samples.add(
        new Sample(
            name + SAMPLE_SUFFIX_SUM, labelNames, labelValues, doubleHistogramPointData.getSum()));

    List<String> labelNamesWithLe = new ArrayList<>(labelNames.size() + 1);
    labelNamesWithLe.addAll(labelNames);
    labelNamesWithLe.add(LABEL_NAME_LE);

    long cumulativeCount = 0;
    List<Double> boundaries = doubleHistogramPointData.getBoundaries();
    List<Long> counts = doubleHistogramPointData.getCounts();
    for (int i = 0; i < counts.size(); i++) {
      List<String> labelValuesWithLe = new ArrayList<>(labelValues.size() + 1);
      labelValuesWithLe.addAll(labelValues);
      labelValuesWithLe.add(
          doubleToGoString(i < boundaries.size() ? boundaries.get(i) : Double.POSITIVE_INFINITY));

      cumulativeCount += counts.get(i);
      samples.add(
          new Sample(
              name + SAMPLE_SUFFIX_BUCKET, labelNamesWithLe, labelValuesWithLe, cumulativeCount));
    }
  }

  private static int estimateNumSamples(int numPoints, MetricDataType type) {
    if (type == MetricDataType.SUMMARY) {
      // count + sum + estimated 2 percentiles (default MinMaxSumCount aggregator).
      return numPoints * 4;
    }
    return numPoints;
  }

  private static Collection<? extends PointData> getPoints(MetricData metricData) {
    switch (metricData.getType()) {
      case DOUBLE_GAUGE:
        return metricData.getDoubleGaugeData().getPoints();
      case DOUBLE_SUM:
        return metricData.getDoubleSumData().getPoints();
      case LONG_GAUGE:
        return metricData.getLongGaugeData().getPoints();
      case LONG_SUM:
        return metricData.getLongSumData().getPoints();
      case SUMMARY:
        return metricData.getDoubleSummaryData().getPoints();
      case HISTOGRAM:
        return metricData.getDoubleHistogramData().getPoints();
    }
    return Collections.emptyList();
  }

  private MetricAdapter() {}
}
