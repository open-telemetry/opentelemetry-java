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
import io.opentelemetry.proto.metrics.v1.internal.ResourceMetrics;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceMetrics.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceMetricsMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrl;
  private final InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers;

  /** Returns Marshalers of ResourceMetrics created by grouping the provided metricData. */
  public static ResourceMetricsMarshaler[] create(Collection<MetricData> metricDataList) {
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

    return resourceMetricsMarshalers;
  }

  ResourceMetricsMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationLibraryMetricsMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationLibraryMetricsMarshalers = instrumentationLibraryMetricsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS, instrumentationLibraryMetricsMarshalers);
    output.serializeString(ResourceMetrics.SCHEMA_URL, schemaUrl);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationLibraryMetricsMarshaler[] instrumentationLibraryMetricsMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceMetrics.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceMetrics.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceMetrics.INSTRUMENTATION_LIBRARY_METRICS,
            instrumentationLibraryMetricsMarshalers);
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
