/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
import static io.opentelemetry.proto.metrics.v1.AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;

import io.opentelemetry.api.metrics.common.Labels;
import io.opentelemetry.proto.common.v1.StringKeyValue;
import io.opentelemetry.proto.metrics.v1.AggregationTemporality;
import io.opentelemetry.proto.metrics.v1.DoubleDataPoint;
import io.opentelemetry.proto.metrics.v1.DoubleGauge;
import io.opentelemetry.proto.metrics.v1.DoubleHistogram;
import io.opentelemetry.proto.metrics.v1.DoubleHistogramDataPoint;
import io.opentelemetry.proto.metrics.v1.DoubleSum;
import io.opentelemetry.proto.metrics.v1.DoubleSummary;
import io.opentelemetry.proto.metrics.v1.DoubleSummaryDataPoint;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.IntDataPoint;
import io.opentelemetry.proto.metrics.v1.IntGauge;
import io.opentelemetry.proto.metrics.v1.IntSum;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
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
        instrumentationLibraryMetrics.add(
            InstrumentationLibraryMetrics.newBuilder()
                .setInstrumentationLibrary(
                    CommonAdapter.toProtoInstrumentationLibrary(entryLibrary.getKey()))
                .addAllMetrics(entryLibrary.getValue())
                .build());
      }
      resourceMetrics.add(
          ResourceMetrics.newBuilder()
              .setResource(ResourceAdapter.toProtoResource(entryResource.getKey()))
              .addAllInstrumentationLibraryMetrics(instrumentationLibraryMetrics)
              .build());
    }
    return resourceMetrics;
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
        builder.setIntSum(
            IntSum.newBuilder()
                .setIsMonotonic(longSumData.isMonotonic())
                .setAggregationTemporality(
                    mapToTemporality(longSumData.getAggregationTemporality()))
                .addAllDataPoints(toIntDataPoints(longSumData.getPoints()))
                .build());
        break;
      case DOUBLE_SUM:
        DoubleSumData doubleSumData = metricData.getDoubleSumData();
        builder.setDoubleSum(
            DoubleSum.newBuilder()
                .setIsMonotonic(doubleSumData.isMonotonic())
                .setAggregationTemporality(
                    mapToTemporality(doubleSumData.getAggregationTemporality()))
                .addAllDataPoints(toDoubleDataPoints(doubleSumData.getPoints()))
                .build());
        break;
      case SUMMARY:
        DoubleSummaryData doubleSummaryData = metricData.getDoubleSummaryData();
        builder.setDoubleSummary(
            DoubleSummary.newBuilder()
                .addAllDataPoints(toSummaryDataPoints(doubleSummaryData.getPoints()))
                .build());
        break;
      case LONG_GAUGE:
        LongGaugeData longGaugeData = metricData.getLongGaugeData();
        builder.setIntGauge(
            IntGauge.newBuilder()
                .addAllDataPoints(toIntDataPoints(longGaugeData.getPoints()))
                .build());
        break;
      case DOUBLE_GAUGE:
        DoubleGaugeData doubleGaugeData = metricData.getDoubleGaugeData();
        builder.setDoubleGauge(
            DoubleGauge.newBuilder()
                .addAllDataPoints(toDoubleDataPoints(doubleGaugeData.getPoints()))
                .build());
        break;
      case HISTOGRAM:
        DoubleHistogramData doubleHistogramData = metricData.getDoubleHistogramData();
        builder.setDoubleHistogram(
            DoubleHistogram.newBuilder()
                .setAggregationTemporality(
                    mapToTemporality(doubleHistogramData.getAggregationTemporality()))
                .addAllDataPoints(toDoubleHistogramDataPoints(doubleHistogramData.getPoints()))
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

  static List<IntDataPoint> toIntDataPoints(Collection<LongPointData> points) {
    List<IntDataPoint> result = new ArrayList<>(points.size());
    for (LongPointData longPoint : points) {
      IntDataPoint.Builder builder =
          IntDataPoint.newBuilder()
              .setStartTimeUnixNano(longPoint.getStartEpochNanos())
              .setTimeUnixNano(longPoint.getEpochNanos())
              .setValue(longPoint.getValue());
      Collection<StringKeyValue> labels = toProtoLabels(longPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<DoubleDataPoint> toDoubleDataPoints(Collection<DoublePointData> points) {
    List<DoubleDataPoint> result = new ArrayList<>(points.size());
    for (DoublePointData doublePoint : points) {
      DoubleDataPoint.Builder builder =
          DoubleDataPoint.newBuilder()
              .setStartTimeUnixNano(doublePoint.getStartEpochNanos())
              .setTimeUnixNano(doublePoint.getEpochNanos())
              .setValue(doublePoint.getValue());
      Collection<StringKeyValue> labels = toProtoLabels(doublePoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static List<DoubleSummaryDataPoint> toSummaryDataPoints(
      Collection<DoubleSummaryPointData> points) {
    List<DoubleSummaryDataPoint> result = new ArrayList<>(points.size());
    for (DoubleSummaryPointData doubleSummaryPoint : points) {
      DoubleSummaryDataPoint.Builder builder =
          DoubleSummaryDataPoint.newBuilder()
              .setStartTimeUnixNano(doubleSummaryPoint.getStartEpochNanos())
              .setTimeUnixNano(doubleSummaryPoint.getEpochNanos())
              .setCount(doubleSummaryPoint.getCount())
              .setSum(doubleSummaryPoint.getSum());
      List<StringKeyValue> labels = toProtoLabels(doubleSummaryPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      // Not calling directly addAllQuantileValues because that generates couple of unnecessary
      // allocations if empty list.
      if (!doubleSummaryPoint.getPercentileValues().isEmpty()) {
        for (ValueAtPercentile valueAtPercentile : doubleSummaryPoint.getPercentileValues()) {
          builder.addQuantileValues(
              DoubleSummaryDataPoint.ValueAtQuantile.newBuilder()
                  .setQuantile(valueAtPercentile.getPercentile() / 100.0)
                  .setValue(valueAtPercentile.getValue())
                  .build());
        }
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<DoubleHistogramDataPoint> toDoubleHistogramDataPoints(
      Collection<DoubleHistogramPointData> points) {
    List<DoubleHistogramDataPoint> result = new ArrayList<>(points.size());
    for (DoubleHistogramPointData doubleHistogramPoint : points) {
      DoubleHistogramDataPoint.Builder builder =
          DoubleHistogramDataPoint.newBuilder()
              .setStartTimeUnixNano(doubleHistogramPoint.getStartEpochNanos())
              .setTimeUnixNano(doubleHistogramPoint.getEpochNanos())
              .setCount(doubleHistogramPoint.getCount())
              .setSum(doubleHistogramPoint.getSum())
              .addAllBucketCounts(doubleHistogramPoint.getCounts());
      List<Double> boundaries = doubleHistogramPoint.getBoundaries();
      if (!boundaries.isEmpty()) {
        builder.addAllExplicitBounds(boundaries);
      }
      Collection<StringKeyValue> labels = toProtoLabels(doubleHistogramPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  @SuppressWarnings("MixedMutabilityReturnType")
  static List<StringKeyValue> toProtoLabels(Labels labels) {
    if (labels.isEmpty()) {
      return Collections.emptyList();
    }
    final List<StringKeyValue> result = new ArrayList<>(labels.size());
    labels.forEach(
        (key, value) ->
            result.add(StringKeyValue.newBuilder().setKey(key).setValue(value).build()));
    return result;
  }

  private MetricAdapter() {}
}
