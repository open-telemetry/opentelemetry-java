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

package io.opentelemetry.exporters.otlp;

import io.opentelemetry.common.Labels;
import io.opentelemetry.common.ReadableKeyValuePairs.KeyValueConsumer;
import io.opentelemetry.proto.common.v1.StringKeyValue;
import io.opentelemetry.proto.metrics.v1.DoubleDataPoint;
import io.opentelemetry.proto.metrics.v1.InstrumentationLibraryMetrics;
import io.opentelemetry.proto.metrics.v1.Int64DataPoint;
import io.opentelemetry.proto.metrics.v1.Metric;
import io.opentelemetry.proto.metrics.v1.MetricDescriptor;
import io.opentelemetry.proto.metrics.v1.MetricDescriptor.Temporality;
import io.opentelemetry.proto.metrics.v1.ResourceMetrics;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint;
import io.opentelemetry.proto.metrics.v1.SummaryDataPoint.ValueAtPercentile;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.data.MetricData.Descriptor;
import io.opentelemetry.sdk.metrics.data.MetricData.DoublePoint;
import io.opentelemetry.sdk.metrics.data.MetricData.LongPoint;
import io.opentelemetry.sdk.metrics.data.MetricData.Point;
import io.opentelemetry.sdk.metrics.data.MetricData.SummaryPoint;
import io.opentelemetry.sdk.resources.Resource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class MetricAdapter {
  static List<ResourceMetrics> toProtoResourceMetrics(Collection<MetricData> metricData) {
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
      Resource resource = metricData.getResource();
      Map<InstrumentationLibraryInfo, List<Metric>> libraryInfoListMap =
          result.get(metricData.getResource());
      if (libraryInfoListMap == null) {
        libraryInfoListMap = new HashMap<>();
        result.put(resource, libraryInfoListMap);
      }
      List<Metric> metricList = libraryInfoListMap.get(metricData.getInstrumentationLibraryInfo());
      if (metricList == null) {
        metricList = new ArrayList<>();
        libraryInfoListMap.put(metricData.getInstrumentationLibraryInfo(), metricList);
      }
      metricList.add(toProtoMetric(metricData));
    }
    return result;
  }

  static Metric toProtoMetric(MetricData metricData) {
    Metric.Builder builder =
        Metric.newBuilder()
            .setMetricDescriptor(toProtoMetricDescriptor(metricData.getDescriptor()));

    // If no points available then return.
    if (metricData.getPoints().isEmpty()) {
      return builder.build();
    }
    switch (builder.getMetricDescriptor().getType()) {
      case UNRECOGNIZED:
      case INVALID_TYPE:
        break;
      case MONOTONIC_INT64:
      case INT64:
        builder.addAllInt64DataPoints(
            toInt64DataPoints(metricData.getPoints(), metricData.getDescriptor()));
        break;
      case MONOTONIC_DOUBLE:
      case DOUBLE:
        builder.addAllDoubleDataPoints(
            toDoubleDataPoints(metricData.getPoints(), metricData.getDescriptor()));
        break;
      case HISTOGRAM:
        // TODO: Add support for histogram.
        break;
      case SUMMARY:
        builder.addAllSummaryDataPoints(
            toSummaryDataPoints(metricData.getPoints(), metricData.getDescriptor()));
        break;
    }
    return builder.build();
  }

  static MetricDescriptor toProtoMetricDescriptor(Descriptor descriptor) {
    return MetricDescriptor.newBuilder()
        .setName(descriptor.getName())
        .setDescription(descriptor.getDescription())
        .setUnit(descriptor.getUnit())
        .setType(toProtoMetricDescriptorType(descriptor.getType()))
        .setTemporality(mapToTemporality(descriptor))
        .build();
  }

  private static Temporality mapToTemporality(Descriptor descriptor) {
    switch (descriptor.getType()) {
      case NON_MONOTONIC_LONG:
      case NON_MONOTONIC_DOUBLE:
      case MONOTONIC_LONG:
      case MONOTONIC_DOUBLE:
        return Temporality.CUMULATIVE;
      case SUMMARY:
        return Temporality.DELTA;
    }
    return Temporality.UNRECOGNIZED;
  }

  static Collection<Int64DataPoint> toInt64DataPoints(
      Collection<Point> points, Descriptor descriptor) {
    List<Int64DataPoint> result = new ArrayList<>(points.size());
    for (Point point : points) {
      LongPoint longPoint = (LongPoint) point;
      Int64DataPoint.Builder builder =
          Int64DataPoint.newBuilder()
              .setStartTimeUnixNano(longPoint.getStartEpochNanos())
              .setTimeUnixNano(longPoint.getEpochNanos())
              .setValue(longPoint.getValue());
      // Not calling directly addAllLabels because that generates couple of unnecessary allocations.
      if (descriptor.getConstantLabels() != null && !descriptor.getConstantLabels().isEmpty()) {
        builder.addAllLabels(toProtoLabels(descriptor.getConstantLabels()));
      }
      Collection<StringKeyValue> labels = toProtoLabels(longPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<DoubleDataPoint> toDoubleDataPoints(
      Collection<Point> points, Descriptor descriptor) {
    List<DoubleDataPoint> result = new ArrayList<>(points.size());
    for (Point point : points) {
      DoublePoint doublePoint = (DoublePoint) point;
      DoubleDataPoint.Builder builder =
          DoubleDataPoint.newBuilder()
              .setStartTimeUnixNano(doublePoint.getStartEpochNanos())
              .setTimeUnixNano(doublePoint.getEpochNanos())
              .setValue(doublePoint.getValue());
      // Not calling directly addAllLabels because that generates couple of unnecessary allocations.
      if (descriptor.getConstantLabels() != null && !descriptor.getConstantLabels().isEmpty()) {
        builder.addAllLabels(toProtoLabels(descriptor.getConstantLabels()));
      }
      Collection<StringKeyValue> labels = toProtoLabels(doublePoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      result.add(builder.build());
    }
    return result;
  }

  static Collection<SummaryDataPoint> toSummaryDataPoints(
      Collection<Point> points, Descriptor descriptor) {
    List<SummaryDataPoint> result = new ArrayList<>(points.size());
    for (Point point : points) {
      SummaryPoint summaryPoint = (SummaryPoint) point;
      SummaryDataPoint.Builder builder =
          SummaryDataPoint.newBuilder()
              .setStartTimeUnixNano(summaryPoint.getStartEpochNanos())
              .setTimeUnixNano(summaryPoint.getEpochNanos())
              .setCount(summaryPoint.getCount())
              .setSum(summaryPoint.getSum());
      // Not calling directly addAllLabels because that generates couple of unnecessary allocations
      // if empty list.
      if (descriptor.getConstantLabels() != null && !descriptor.getConstantLabels().isEmpty()) {
        builder.addAllLabels(toProtoLabels(descriptor.getConstantLabels()));
      }
      Collection<StringKeyValue> labels = toProtoLabels(summaryPoint.getLabels());
      if (!labels.isEmpty()) {
        builder.addAllLabels(labels);
      }
      // Not calling directly addAllPercentileValues because that generates couple of unnecessary
      // allocations if empty list.
      List<ValueAtPercentile> valueAtPercentiles =
          toProtoValueAtPercentiles(summaryPoint.getPercentileValues());
      if (!valueAtPercentiles.isEmpty()) {
        builder.addAllPercentileValues(valueAtPercentiles);
      }
      result.add(builder.build());
    }
    return result;
  }

  // TODO: Consider to pass the Builder and directly add values.
  @SuppressWarnings("MixedMutabilityReturnType")
  static List<ValueAtPercentile> toProtoValueAtPercentiles(
      Collection<MetricData.ValueAtPercentile> valueAtPercentiles) {
    if (valueAtPercentiles.isEmpty()) {
      return Collections.emptyList();
    }
    List<ValueAtPercentile> result = new ArrayList<>(valueAtPercentiles.size());
    for (MetricData.ValueAtPercentile valueAtPercentile : valueAtPercentiles) {
      result.add(
          ValueAtPercentile.newBuilder()
              .setPercentile(valueAtPercentile.getPercentile())
              .setValue(valueAtPercentile.getValue())
              .build());
    }
    return result;
  }

  static MetricDescriptor.Type toProtoMetricDescriptorType(Descriptor.Type descriptorType) {
    switch (descriptorType) {
      case NON_MONOTONIC_LONG:
        return MetricDescriptor.Type.INT64;
      case NON_MONOTONIC_DOUBLE:
        return MetricDescriptor.Type.DOUBLE;
      case MONOTONIC_LONG:
        return MetricDescriptor.Type.MONOTONIC_INT64;
      case MONOTONIC_DOUBLE:
        return MetricDescriptor.Type.MONOTONIC_DOUBLE;
      case SUMMARY:
        return MetricDescriptor.Type.SUMMARY;
    }
    return MetricDescriptor.Type.UNRECOGNIZED;
  }

  @SuppressWarnings("MixedMutabilityReturnType")
  static Collection<StringKeyValue> toProtoLabels(Labels labels) {
    if (labels.isEmpty()) {
      return Collections.emptyList();
    }
    final List<StringKeyValue> result = new ArrayList<>(labels.size());
    labels.forEach(
        new KeyValueConsumer<String>() {
          @Override
          public void consume(String key, String value) {
            result.add(StringKeyValue.newBuilder().setKey(key).setValue(value).build());
          }
        });
    return result;
  }

  private MetricAdapter() {}
}
