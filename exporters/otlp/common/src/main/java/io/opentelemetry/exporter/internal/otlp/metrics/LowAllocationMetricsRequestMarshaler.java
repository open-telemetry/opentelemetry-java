/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.AbstractResourceScopeMapSizeCalculator;
import io.opentelemetry.exporter.internal.otlp.AbstractResourceScopeMapWriter;
import io.opentelemetry.proto.collector.metrics.v1.internal.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.internal.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link MetricData} to OTLP ExportMetricsServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LowAllocationMetricsRequestMarshaler extends Marshaler {

  private final MarshalerContext context = new MarshalerContext();

  @SuppressWarnings("NullAway")
  private Map<Resource, Map<InstrumentationScopeInfo, List<MetricData>>> resourceAndScopeMap;

  private int size;

  public void initialize(Collection<MetricData> metricDataList) {
    resourceAndScopeMap = groupByResourceAndScope(context, metricDataList);
    size = calculateSize(context, resourceAndScopeMap);
  }

  public void reset() {
    context.reset();
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  private final ResourceScopeMapWriter resourceScopeMapWriter = new ResourceScopeMapWriter();

  @Override
  public void writeTo(Serializer output) {
    // serializing can be retried, reset the indexes, so we could call writeTo multiple times
    context.resetReadIndex();
    resourceScopeMapWriter.initialize(
        output, ExportMetricsServiceRequest.RESOURCE_METRICS, context);
    resourceAndScopeMap.forEach(resourceScopeMapWriter);
  }

  private static int calculateSize(
      MarshalerContext context,
      Map<Resource, Map<InstrumentationScopeInfo, List<MetricData>>> resourceAndScopeMap) {
    if (resourceAndScopeMap.isEmpty()) {
      return 0;
    }

    ResourceScopeMapSizeCalculator resourceScopeMapSizeCalculator =
        context.getInstance(
            ResourceScopeMapSizeCalculator.class, ResourceScopeMapSizeCalculator::new);
    resourceScopeMapSizeCalculator.initialize(ExportTraceServiceRequest.RESOURCE_SPANS, context);
    resourceAndScopeMap.forEach(resourceScopeMapSizeCalculator);

    return resourceScopeMapSizeCalculator.getSize();
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<MetricData>>>
      groupByResourceAndScope(MarshalerContext context, Collection<MetricData> metricDataList) {

    if (metricDataList.isEmpty()) {
      return Collections.emptyMap();
    }

    return MarshalerUtil.groupByResourceAndScope(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationScopeInfo,
        context);
  }

  private static class ResourceScopeMapWriter extends AbstractResourceScopeMapWriter<MetricData> {

    @Override
    protected void handle(
        Map<InstrumentationScopeInfo, List<MetricData>> instrumentationScopeInfoListMap)
        throws IOException {
      ResourceMetricsMarshaler.writeTo(output, instrumentationScopeInfoListMap, context);
    }
  }

  private static class ResourceScopeMapSizeCalculator
      extends AbstractResourceScopeMapSizeCalculator<MetricData> {

    @Override
    public int calculateSize(
        Resource resource,
        Map<InstrumentationScopeInfo, List<MetricData>> instrumentationScopeInfoListMap) {
      return ResourceMetricsMarshaler.calculateSize(
          context, resource, instrumentationScopeInfoListMap);
    }
  }
}
