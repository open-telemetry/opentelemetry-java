/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ResourceMarshaler;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.collector.metrics.v1.internal.ExportMetricsServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link MetricData} to OTLP ExportMetricsServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class MetricsRequestMarshaler extends MarshalerWithSize {

  private final ResourceMetricsMarshaler[] resourceMetricsMarshalers;

  /**
   * Returns a {@link MetricsRequestMarshaler} that can be used to convert the provided {@link
   * MetricData} into a serialized OTLP ExportMetricsServiceRequest.
   */
  public static MetricsRequestMarshaler create(Collection<MetricData> metricDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(metricDataList);

    ResourceMetricsMarshaler[] resourceMetricsMarshalers =
        new ResourceMetricsMarshaler[resourceAndLibraryMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>> entry :
        resourceAndLibraryMap.entrySet()) {
      final InstrumentationLibraryMetricsMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationLibraryMetricsMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationLibraryInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationLibraryMetricsMarshaler(
                InstrumentationLibraryMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceMetricsMarshalers[posResource++] =
          new ResourceMetricsMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return new MetricsRequestMarshaler(resourceMetricsMarshalers);
  }

  private MetricsRequestMarshaler(ResourceMetricsMarshaler[] resourceMetricsMarshalers) {
    super(calculateSize(resourceMetricsMarshalers));
    this.resourceMetricsMarshalers = resourceMetricsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportMetricsServiceRequest.RESOURCE_METRICS, resourceMetricsMarshalers);
  }

  private static int calculateSize(ResourceMetricsMarshaler[] resourceMetricsMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExportMetricsServiceRequest.RESOURCE_METRICS, resourceMetricsMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>>
      groupByResourceAndLibrary(Collection<MetricData> metricDataList) {
    return MarshalerUtil.groupByResourceAndLibrary(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationLibraryInfo,
        MetricMarshaler::create);
  }
}
