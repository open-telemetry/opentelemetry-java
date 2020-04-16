/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.prometheus;

import static io.prometheus.client.Collector.doubleToGoString;

import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.ValueAtPercentile;
import io.prometheus.client.Collector;
import io.prometheus.client.Collector.MetricFamilySamples;
import io.prometheus.client.Collector.MetricFamilySamples.Sample;
import io.prometheus.client.Collector.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
  static final String LABEL_NAME_QUANTILE = "quantile";

  // Converts a MetricData to a Prometheus MetricFamilySamples.
  static MetricFamilySamples toMetricFamilySamples(MetricData metricData) {
    Descriptor descriptor = metricData.getDescriptor();
    String fullName =
        toMetricFullName(
            descriptor.getName(), metricData.getInstrumentationLibraryInfo().getName());
    Type type = toMetricFamilyType(descriptor.getType());

    return new MetricFamilySamples(
        fullName,
        type,
        descriptor.getDescription(),
        toSamples(fullName, descriptor, metricData.getPoints()));
  }

  private static String toMetricFullName(
      String descriptorMetricName, String instrumentationLibraryName) {
    if (instrumentationLibraryName.isEmpty()) {
      return Collector.sanitizeMetricName(descriptorMetricName);
    }

    // Use "_" here even though the right way would be to use "." in general, but "." will be
    // replaced with "_" anyway so one less replace call.
    return Collector.sanitizeMetricName(instrumentationLibraryName + "_" + descriptorMetricName);
  }

  static Type toMetricFamilyType(MetricData.Descriptor.Type type) {
    switch (type) {
      case NON_MONOTONIC_LONG:
      case NON_MONOTONIC_DOUBLE:
        return Type.GAUGE;
      case MONOTONIC_LONG:
      case MONOTONIC_DOUBLE:
        return Type.COUNTER;
      case SUMMARY:
        return Type.SUMMARY;
    }
    return Type.UNTYPED;
  }

  // Converts a list of points from MetricData to a list of Prometheus Samples.
  static List<Sample> toSamples(String name, Descriptor descriptor, Collection<Point> points) {
    final List<Sample> samples =
        new ArrayList<>(estimateNumSamples(points.size(), descriptor.getType()));

    List<String> constLabelNames = Collections.emptyList();
    List<String> constLabelValues = Collections.emptyList();
    if (descriptor.getConstantLabels().size() != 0) {
      constLabelNames = new ArrayList<>(descriptor.getConstantLabels().size());
      constLabelValues = new ArrayList<>(descriptor.getConstantLabels().size());
      for (Map.Entry<String, String> entry : descriptor.getConstantLabels().entrySet()) {
        constLabelNames.add(toLabelName(entry.getKey()));
        constLabelValues.add(entry.getValue() == null ? "" : entry.getValue());
      }
    }

    for (Point point : points) {
      List<String> labelNames = Collections.emptyList();
      List<String> labelValues = Collections.emptyList();
      if (constLabelNames.size() + point.getLabels().size() != 0) {
        labelNames =
            new ArrayList<>(descriptor.getConstantLabels().size() + point.getLabels().size());
        labelNames.addAll(constLabelNames);
        labelValues =
            new ArrayList<>(descriptor.getConstantLabels().size() + point.getLabels().size());
        labelValues.addAll(constLabelValues);

        for (Map.Entry<String, String> entry : point.getLabels().entrySet()) {
          // TODO: Use a cache(map) of converted label names to avoid sanitization multiple times
          // for the same label key.
          labelNames.add(toLabelName(entry.getKey()));
          labelValues.add(entry.getValue() == null ? "" : entry.getValue());
        }
      }

      switch (descriptor.getType()) {
        case MONOTONIC_DOUBLE:
        case NON_MONOTONIC_DOUBLE:
          DoublePoint doublePoint = (DoublePoint) point;
          samples.add(new Sample(name, labelNames, labelValues, doublePoint.getValue()));
          break;
        case MONOTONIC_LONG:
        case NON_MONOTONIC_LONG:
          LongPoint longPoint = (LongPoint) point;
          samples.add(new Sample(name, labelNames, labelValues, longPoint.getValue()));
          break;
        case SUMMARY:
          addSummarySamples((SummaryPoint) point, name, labelNames, labelValues, samples);
          break;
      }
    }
    return samples;
  }

  // Converts a label keys to a label names. Sanitizes the label keys.
  static String toLabelName(String labelKey) {
    return Collector.sanitizeMetricName(labelKey);
  }

  private static void addSummarySamples(
      SummaryPoint summaryPoint,
      String name,
      List<String> labelNames,
      List<String> labelValues,
      List<Sample> samples) {
    samples.add(
        new Sample(name + SAMPLE_SUFFIX_COUNT, labelNames, labelValues, summaryPoint.getCount()));
    samples.add(
        new Sample(name + SAMPLE_SUFFIX_SUM, labelNames, labelValues, summaryPoint.getSum()));
    List<ValueAtPercentile> valueAtPercentiles = summaryPoint.getPercentileValues();
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

  private static int estimateNumSamples(int numPoints, Descriptor.Type type) {
    switch (type) {
      case NON_MONOTONIC_LONG:
      case NON_MONOTONIC_DOUBLE:
      case MONOTONIC_LONG:
      case MONOTONIC_DOUBLE:
        return numPoints;
      case SUMMARY:
        // count + sum + estimated 2 percentiles (default MinMaxSumCount aggregator).
        return numPoints * 4;
    }
    return numPoints;
  }

  private MetricAdapter() {}
}
