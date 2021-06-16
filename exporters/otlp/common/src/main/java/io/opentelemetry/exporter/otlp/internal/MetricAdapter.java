/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.proto.common.v1.AnyValue;
import io.opentelemetry.proto.common.v1.KeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.Gauge;
import io.opentelemetry.proto.metrics.v1.Histogram;
import io.opentelemetry.proto.metrics.v1.HistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.NumberDataPoint;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.Sum;
import io.opentelemetry.proto.metrics.v1.Summary;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.DoubleGaugeData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.DoubleSumData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryData;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import io.opentelemetry.sdk.metrics.data.LongGaugeData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.LongSumData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Converter from SDK {@link MetricData} to OTLP {@link ResourceMetrics}. */
public final class MetricAdapter {

  /** Converts the provided {@link MetricData} to {@link ResourceMetrics}. */
  public static List<ResourceMetrics> toProtoResourceMetrics(Collection<MetricData> metricData) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Metric>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(metricData);
    List<ResourceMetrics> resourceMetrics = new ArrayList<>(resourceAndLibraryMap.size());
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Metric>>> entryResource :
        resourceAndLibraryMap.entrySet()) {
      List<InstrumentationLibraryMetrics> instrumentationLibraryMetrics =
          new ArrayList<>(entryResource.getValue().size());
      for (Map.Entry<InstrumentationLibraryInfo, List<Metric>> entryLibrary :
          entryResource.getValue().entrySet()) {
        instrumentationLibraryMetrics.add(buildInstrumentationLibraryMetrics(entryLibrary));
      }
      resourceMetrics.add(
          ResourceMetrics.newBuilder()
              .setResource(ResourceAdapter.toProtoResource(entryResource.getKey()))
              .addAllInstrumentationLibraryMetrics(instrumentationLibraryMetrics)
              .build());
    }
    return resourceMetrics;
  }

  private static InstrumentationLibraryMetrics buildInstrumentationLibraryMetrics(
      Map.Entry<InstrumentationLibraryInfo, List<Metric>> entryLibrary) {
    InstrumentationLibraryMetrics.Builder metricsBuilder =
        InstrumentationLibraryMetrics.newBuilder()
            .setInstrumentationLibrary(
                CommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()))
            .addAllMetrics(entryLibrary.getValue());
    if (entryLibrary.getKey().getSchemaUrl() != null) {
      metricsBuilder.setSchemaUrl(entryLibrary.getKey().getSchemaUrl());
    }
    return metricsBuilder.build();
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Metric>>>
      groupByResourceAndLibrary(Collection<MetricData> metricDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Metric>>> result = new HashMap<>();
    for (MetricData metricData : metricDataList) {
      if (metricData.isEmpty()) {
        // If no points available then ignore.
        continue;
      }

      Resource resource = metricData.getResource();
      Map<InstrumentationLibraryInfo, List<Metric>> libraryInfoListMap =
          result.get(metricData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<Metric> metricList =
          libraryInfoListMap.computeIfAbsent(
              metricData.getInstrumentationLibraryInfo(), k -> new ArrayList<>());
      metricList.add(toProtoMetric(metricData));
    }
    return result;
  }

  // fall through comment isn't working for some reason.
  @SuppressWarnings("fallthrough")
  static Metric toProtoMetric(MetricData metricData) {
    Metric.Builder builder =
        Metric.newBuilder()
            .setName(metricData.getName())
            .setDescription(metricData.getDescription())
            .setUnit(metricData.getUnit());

    switch (metricData.getType()) {
      case LONG_SUM:
        LongSumData longSumData = metricData.getLongSumData();
        builder.setSum(
            Sum.newBuilder()
                .setIsMonotonic(longSumData.isMonotonic())
                .setAggregationTemporality(
                    mapToTemporality(longSumData.getAggregationTemporality()))
                .addAllDataPoints(toIntDataPoints(longSumData.getPoints()))
                .build());
        break;
      case DOUBLE_SUM:
        DoubleSumData doubleSumData = metricData.getDoubleSumData();
        builder.setSum(
            Sum.newBuilder()
                .setIsMonotonic(doubleSumData.isMonotonic())
                .setAggregationTemporality(
                    mapToTemporality(doubleSumData.getAggregationTemporality()))
                .addAllDataPoints(toDoubleDataPoints(doubleSumData.getPoints()))
                .build());
        break;
      case SUMMARY:
        DoubleSummaryData doubleSummaryData = metricData.getDoubleSummaryData();
        builder.setSummary(
            Summary.newBuilder()
                .addAllDataPoints(toSummaryDataPoints(doubleSummaryData.getPoints()))
                .build());
        break;
      case LONG_GAUGE:
        LongGaugeData longGaugeData = metricData.getLongGaugeData();
        builder.setGauge(
            Gauge.newBuilder()
                .addAllDataPoints(toIntDataPoints(longGaugeData.getPoints()))
                .build());
        break;
      case DOUBLE_GAUGE:
        DoubleGaugeData doubleGaugeData = metricData.getDoubleGaugeData();
        builder.setGauge(
            Gauge.newBuilder()
                .addAllDataPoints(toDoubleDataPoints(doubleGaugeData.getPoints()))
                .build());
        break;
      case HISTOGRAM:
        DoubleHistogramData doubleHistogramData = metricData.getDoubleHistogramData();
        builder.setHistogram(
            Histogram.newBuilder()
                .setAggregationTemporality(
                    mapToTemporality(doubleHistogramData.getAggregationTemporality()))
                .addAllDataPoints(toHistogramDataPoints(doubleHistogramData.getPoints()))
                .build());
        break;
    }
    return builder.build();
  }

  private static AggregationTemporality mapToTemporality(
      io.opentelemetry.sdk.metrics.data.AggregationTemporality temporality) {
    switch (temporality) {
      case CUMULATIVE:
        return AGGREGATION_TEMPORALITY_CUMULATIVE;
      case DELTA:
        return AGGREGATION_TEMPORALITY_DELTA;
    }
    return AGGREGATION_TEMPORALITY_UNSPECIFIED;
  }

  static List<NumberDataPoint> toIntDataPoints(Collection<LongPointData> points) {
    List<NumberDataPoint> result = new ArrayList<>(points.size());
    for (LongPointData longPoint : points) {
      NumberDataPoint.Builder builder =
          NumberDataPoint.newBuilder()
              .setStartTimeUnixNano(longPoint.getStartEpochNanos())
              .setTimeUnixNano(longPoint.getEpochNanos())
              .setAsInt(longPoint.getValue());
      Collection<KeyValue> labels = toProtoLabels(longPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<NumberDataPoint> toDoubleDataPoints(Collection<DoublePointData> points) {
    List<NumberDataPoint> result = new ArrayList<>(points.size());
    for (DoublePointData doublePoint : points) {
      NumberDataPoint.Builder builder =
          NumberDataPoint.newBuilder()
              .setStartTimeUnixNano(doublePoint.getStartEpochNanos())
              .setTimeUnixNano(doublePoint.getEpochNanos())
              .setAsDouble(doublePoint.getValue());
      Collection<KeyValue> labels = toProtoLabels(doublePoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static List<SummaryDataPoint> toSummaryDataPoints(Collection<DoubleSummaryPointData> points) {
    List<SummaryDataPoint> result = new ArrayList<>(points.size());
    for (DoubleSummaryPointData doubleSummaryPoint : points) {
      SummaryDataPoint.Builder builder =
          SummaryDataPoint.newBuilder()
              .setStartTimeUnixNano(doubleSummaryPoint.getStartEpochNanos())
              .setTimeUnixNano(doubleSummaryPoint.getEpochNanos())
              .setCount(doubleSummaryPoint.getCount())
              .setSum(doubleSummaryPoint.getSum());
      List<KeyValue> labels = toProtoLabels(doubleSummaryPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      // Not calling directly addAllQuantileValues because that generates couple of unnecessary
      // allocations if empty list.
      if (!doubleSummaryPoint.getPercentileValues().isEmpty()) {
        for (ValueAtPercentile valueAtPercentile : doubleSummaryPoint.getPercentileValues()) {
          builder.addQuantileValues(
              SummaryDataPoint.ValueAtQuantile.newBuilder()
                  .setQuantile(valueAtPercentile.getPercentile() / 100.0)
                  .setValue(valueAtPercentile.getValue())
                  .build());
        }
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<HistogramDataPoint> toHistogramDataPoints(
      Collection<DoubleHistogramPointData> points) {
    List<HistogramDataPoint> result = new ArrayList<>(points.size());
    for (DoubleHistogramPointData doubleHistogramPoint : points) {
      HistogramDataPoint.Builder builder =
          HistogramDataPoint.newBuilder()
              .setStartTimeUnixNano(doubleHistogramPoint.getStartEpochNanos())
              .setTimeUnixNano(doubleHistogramPoint.getEpochNanos())
              .setCount(doubleHistogramPoint.getCount())
              .setSum(doubleHistogramPoint.getSum())
              .addAllBucketCounts(doubleHistogramPoint.getCounts());
      List<Double> boundaries = doubleHistogramPoint.getBoundaries();
      if (!boundaries.isEmpty()) {
        builder.addAllExplicitBounds(boundaries);
      }
      Collection<KeyValue> labels = toProtoLabels(doubleHistogramPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllAttributes(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  @SuppressWarnings("MixedMutabilityReturnType")
  static List<KeyValue> toProtoLabels(Labels labels) {
    if (labels.isEmpty()) {
      return Collections.emptyList();
    }
    final List<KeyValue> result = new ArrayList<>(labels.size());
    labels.forEach(
        (key, value) ->
            result.add(
                KeyValue.newBuilder()
                    .setKey(key)
                    .setValue(AnyValue.newBuilder().setStringValue(value).build())
                    .build()));
    return result;
  }

  private MetricAdapter() {}
}
