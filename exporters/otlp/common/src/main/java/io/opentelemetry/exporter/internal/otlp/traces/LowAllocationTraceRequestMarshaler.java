/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.collector.trace.v1.internal.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link SpanData} to OTLP ExportTraceServiceRequest. See {@link
 * TraceRequestMarshaler}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * void marshal(LowAllocationTraceRequestMarshaler requestMarshaler, OutputStream output,
 *     List<SpanData> spanList) throws IOException {
 *   requestMarshaler.initialize(spanList);
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
public final class LowAllocationTraceRequestMarshaler extends Marshaler {
  private static final MarshalerContext.Key RESOURCE_SPAN_SIZE_CALCULATOR_KEY =
      MarshalerContext.key();
  private static final MarshalerContext.Key RESOURCE_SPAN_WRITER_KEY = MarshalerContext.key();

  private final MarshalerContext context = new MarshalerContext();

  @SuppressWarnings("NullAway")
  private Map<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> resourceAndScopeMap;

  private int size;

  public void initialize(Collection<SpanData> spanDataList) {
    resourceAndScopeMap = groupByResourceAndScope(context, spanDataList);
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
        ExportTraceServiceRequest.RESOURCE_SPANS,
        resourceAndScopeMap,
        ResourceSpansStatelessMarshaler.INSTANCE,
        context,
        RESOURCE_SPAN_WRITER_KEY);
  }

  private static int calculateSize(
      MarshalerContext context,
      Map<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> resourceAndScopeMap) {
    return StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
        ExportTraceServiceRequest.RESOURCE_SPANS,
        resourceAndScopeMap,
        ResourceSpansStatelessMarshaler.INSTANCE,
        context,
        RESOURCE_SPAN_SIZE_CALCULATOR_KEY);
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<SpanData>>>
      groupByResourceAndScope(MarshalerContext context, Collection<SpanData> spanDataList) {

    if (spanDataList.isEmpty()) {
      return Collections.emptyMap();
    }

    return StatelessMarshalerUtil.groupByResourceAndScope(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationScopeInfo,
        context);
  }
}
