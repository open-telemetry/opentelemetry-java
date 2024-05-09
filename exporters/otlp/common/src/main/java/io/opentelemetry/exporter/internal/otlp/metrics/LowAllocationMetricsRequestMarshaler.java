/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.collector.metrics.v1.internal.ExportMetricsServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link MetricData} to OTLP ExportMetricsServiceRequest. See
 * {@link MetricsRequestMarshaler}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * void marshal(LowAllocationMetricsRequestMarshaler requestMarshaler, OutputStream output,
 *     Collection<MetricData> metricDataList) throws IOException {
 *   requestMarshaler.initialize(metricDataList);
 *   try {
 *     requestMarshaler.writeBinaryTo(output);
 *   } finally {
 *     requestMarshaler.reset();
 *   }
 * }
 * }</pre>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LowAllocationMetricsRequestMarshaler extends Marshaler {
  private static final MarshalerContext.Key RESOURCE_METRIC_SIZE_CALCULATOR_KEY =
      MarshalerContext.key();
  private static final MarshalerContext.Key RESOURCE_METRIC_WRITER_KEY = MarshalerContext.key();

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

  @Override
  public void writeTo(Serializer output) throws IOException {
    // serializing can be retried, reset the indexes, so we could call writeTo multiple times
    context.resetReadIndex();
    output.serializeRepeatedMessageWithContext(
        ExportMetricsServiceRequest.RESOURCE_METRICS,
        resourceAndScopeMap,
        ResourceMetricsStatelessMarshaler.INSTANCE,
        context,
        RESOURCE_METRIC_WRITER_KEY);
  }

  private static int calculateSize(
      MarshalerContext context,
      Map<Resource, Map<InstrumentationScopeInfo, List<MetricData>>> resourceAndScopeMap) {
    return StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
        ExportMetricsServiceRequest.RESOURCE_METRICS,
        resourceAndScopeMap,
        ResourceMetricsStatelessMarshaler.INSTANCE,
        context,
        RESOURCE_METRIC_SIZE_CALCULATOR_KEY);
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<MetricData>>>
      groupByResourceAndScope(MarshalerContext context, Collection<MetricData> metricDataList) {

    if (metricDataList.isEmpty()) {
      return Collections.emptyMap();
    }

    return StatelessMarshalerUtil.groupByResourceAndScope(
        metricDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        MetricData::getResource,
        MetricData::getInstrumentationScopeInfo,
        context);
  }
}
