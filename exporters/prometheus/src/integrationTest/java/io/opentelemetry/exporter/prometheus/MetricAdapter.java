/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.prometheus;

import static io.prometheus.client.Collector.doubleToGoString;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.metrics.data.AggregationTemporality;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricDataType;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import javax.annotation.Nullable;

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
        toSamples(cleanMetricName, metricData.getType(), Serializer.getPoints(metricData)));
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
        SumData<LongPointData> longSumData = metricData.getLongSumData();
        if (longSumData.isMonotonic()
            && longSumData.getAggregationTemporality() == AggregationTemporality.CUMULATIVE) {
          return Collector.Type.COUNTER;
        }
        return Collector.Type.GAUGE;
      case DOUBLE_SUM:
        SumData<DoublePointData> doubleSumData = metricData.getDoubleSumData();
        if (doubleSumData.isMonotonic()
            && doubleSumData.getAggregationTemporality() == AggregationTemporality.CUMULATIVE) {
          return Collector.Type.COUNTER;
        }
        return Collector.Type.GAUGE;
      case SUMMARY:
        return Collector.Type.SUMMARY;
      case HISTOGRAM:
        return Collector.Type.HISTOGRAM;
      case EXPONENTIAL_HISTOGRAM:
        return Collector.Type.UNKNOWN; // todo exporter for exponential histogram
    }
    return Collector.Type.UNKNOWN;
  }

  static final Function<String, String> sanitizer = new NameSanitizer();

  // Converts a list of points from MetricData to a list of Prometheus Samples.
  static List<Sample> toSamples(
      String name, MetricDataType type, Collection<? extends PointData> points) {
    List<Sample> samples = new ArrayList<>(estimateNumSamples(points.size(), type));

    for (PointData pointData : points) {
      Attributes attributes = pointData.getAttributes();
      List<String> labelNames = new ArrayList<>(attributes.size());
      List<String> labelValues = new ArrayList<>(attributes.size());
      attributes.forEach(
          (key, value) -> {
            String sanitizedLabelName = sanitizer.apply(key.getKey());
            labelNames.add(sanitizedLabelName);
            // TODO: We want to create an error-log if there is overlap in toString of attribute
            // values for the same key name.
            labelValues.add(value == null ? "" : value.toString());
          });

      switch (type) {
        case DOUBLE_SUM:
        case DOUBLE_GAUGE:
          DoublePointData doublePoint = (DoublePointData) pointData;
          samples.add(
              createSample(
                  name,
                  labelNames,
                  labelValues,
                  doublePoint.getValue(),
                  // Prometheus doesn't support exemplars on SUM/GAUGE
                  null,
                  doublePoint.getEpochNanos()));
          break;
        case LONG_SUM:
        case LONG_GAUGE:
          LongPointData longPoint = (LongPointData) pointData;
          samples.add(
              createSample(
                  name,
                  labelNames,
                  labelValues,
                  longPoint.getValue(),
                  // Prometheus doesn't support exemplars on SUM/GAUGE
                  null,
                  longPoint.getEpochNanos()));
          break;
        case SUMMARY:
          addSummarySamples((SummaryPointData) pointData, name, labelNames, labelValues, samples);
          break;
        case HISTOGRAM:
          addHistogramSamples(
              (HistogramPointData) pointData, name, labelNames, labelValues, samples);
          break;
        case EXPONENTIAL_HISTOGRAM:
          break; // todo
      }
    }
    return samples;
  }

  private static void addSummarySamples(
      SummaryPointData doubleSummaryPoint,
      String name,
      List<String> labelNames,
      List<String> labelValues,
      List<Sample> samples) {
    samples.add(
        createSample(
            name + SAMPLE_SUFFIX_COUNT,
            labelNames,
            labelValues,
            doubleSummaryPoint.getCount(),
            null,
            doubleSummaryPoint.getEpochNanos()));

    samples.add(
        createSample(
            name + SAMPLE_SUFFIX_SUM,
            labelNames,
            labelValues,
            doubleSummaryPoint.getSum(),
            null,
            doubleSummaryPoint.getEpochNanos()));

    List<ValueAtQuantile> valueAtQuantiles = doubleSummaryPoint.getValues();
    List<String> labelNamesWithQuantile = new ArrayList<>(labelNames.size());
    labelNamesWithQuantile.addAll(labelNames);
    labelNamesWithQuantile.add(LABEL_NAME_QUANTILE);
    for (ValueAtQuantile valueAtQuantile : valueAtQuantiles) {
      List<String> labelValuesWithQuantile = new ArrayList<>(labelValues.size());
      labelValuesWithQuantile.addAll(labelValues);
      labelValuesWithQuantile.add(doubleToGoString(valueAtQuantile.getQuantile()));
      samples.add(
          createSample(
              name,
              labelNamesWithQuantile,
              labelValuesWithQuantile,
              valueAtQuantile.getValue(),
              null,
              doubleSummaryPoint.getEpochNanos()));
    }
  }

  private static void addHistogramSamples(
      HistogramPointData histogramPointData,
      String name,
      List<String> labelNames,
      List<String> labelValues,
      List<Sample> samples) {
    samples.add(
        createSample(
            name + SAMPLE_SUFFIX_COUNT,
            labelNames,
            labelValues,
            histogramPointData.getCount(),
            null,
            histogramPointData.getEpochNanos()));

    samples.add(
        createSample(
            name + SAMPLE_SUFFIX_SUM,
            labelNames,
            labelValues,
            histogramPointData.getSum(),
            null,
            histogramPointData.getEpochNanos()));

    List<String> labelNamesWithLe = new ArrayList<>(labelNames.size() + 1);
    labelNamesWithLe.addAll(labelNames);
    labelNamesWithLe.add(LABEL_NAME_LE);

    long cumulativeCount = 0;
    List<Long> counts = histogramPointData.getCounts();
    for (int i = 0; i < counts.size(); i++) {
      List<String> labelValuesWithLe = new ArrayList<>(labelValues.size() + 1);
      // This is the upper boundary (inclusive). I.e. all values should be < this value (LE -
      // Less-then-or-Equal).
      double boundary = histogramPointData.getBucketUpperBound(i);
      labelValuesWithLe.addAll(labelValues);
      labelValuesWithLe.add(doubleToGoString(boundary));

      cumulativeCount += counts.get(i);
      samples.add(
          createSample(
              name + SAMPLE_SUFFIX_BUCKET,
              labelNamesWithLe,
              labelValuesWithLe,
              cumulativeCount,
              filterExemplars(
                  histogramPointData.getExemplars(),
                  histogramPointData.getBucketLowerBound(i),
                  boundary),
              histogramPointData.getEpochNanos()));
    }
  }

  @Nullable
  private static ExemplarData filterExemplars(
      Collection<ExemplarData> exemplars, double min, double max) {
    ExemplarData result = null;
    for (ExemplarData e : exemplars) {
      double value = getExemplarValue(e);
      if (value <= max && value > min) {
        result = e;
      }
    }
    return result;
  }

  private static int estimateNumSamples(int numPoints, MetricDataType type) {
    if (type == MetricDataType.SUMMARY) {
      // count + sum + estimated 2 percentiles
      return numPoints * 4;
    }
    return numPoints;
  }

  private static Sample createSample(
      String name,
      List<String> labelNames,
      List<String> labelValues,
      double value,
      @Nullable ExemplarData exemplar,
      long timestampNanos) {
    if (exemplar != null) {
      return new Sample(
          name,
          labelNames,
          labelValues,
          value,
          toPrometheusExemplar(exemplar),
          TimeUnit.MILLISECONDS.convert(timestampNanos, TimeUnit.NANOSECONDS));
    }
    return new Sample(
        name,
        labelNames,
        labelValues,
        value,
        null,
        TimeUnit.MILLISECONDS.convert(timestampNanos, TimeUnit.NANOSECONDS));
  }

  private static io.prometheus.client.exemplars.Exemplar toPrometheusExemplar(
      ExemplarData exemplar) {
    SpanContext spanContext = exemplar.getSpanContext();
    if (spanContext.isValid()) {
      return new io.prometheus.client.exemplars.Exemplar(
          getExemplarValue(exemplar),
          // Convert to ms for prometheus, truncate nanosecond precision.
          TimeUnit.NANOSECONDS.toMillis(exemplar.getEpochNanos()),
          "trace_id",
          spanContext.getTraceId(),
          "span_id",
          spanContext.getSpanId());
    }
    return new io.prometheus.client.exemplars.Exemplar(getExemplarValue(exemplar));
  }

  private static double getExemplarValue(ExemplarData exemplar) {
    return exemplar instanceof DoubleExemplarData
        ? ((DoubleExemplarData) exemplar).getValue()
        : (double) ((LongExemplarData) exemplar).getValue();
  }

  private MetricAdapter() {}
}
