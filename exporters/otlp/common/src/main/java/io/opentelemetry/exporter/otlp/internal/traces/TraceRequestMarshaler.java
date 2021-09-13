/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ResourceMarshaler;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.collector.trace.v1.internal.ExportTraceServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link SpanData} to OTLP ExportTraceServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class TraceRequestMarshaler extends MarshalerWithSize {

  private final ResourceSpansMarshaler[] resourceSpansMarshalers;

  /**
   * Returns a {@link TraceRequestMarshaler} that can be used to convert the provided {@link
   * SpanData} into a serialized OTLP ExportTraceServiceRequest.
   */
  public static TraceRequestMarshaler create(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(spanDataList);

    final ResourceSpansMarshaler[] resourceSpansMarshalers =
        new ResourceSpansMarshaler[resourceAndLibraryMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>> entry :
        resourceAndLibraryMap.entrySet()) {
      final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationLibrarySpansMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationLibraryInfo, List<SpanMarshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationLibrarySpansMarshaler(
                InstrumentationLibraryMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceSpansMarshalers[posResource++] =
          new ResourceSpansMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return new TraceRequestMarshaler(resourceSpansMarshalers);
  }

  private TraceRequestMarshaler(ResourceSpansMarshaler[] resourceSpansMarshalers) {
    super(
        MarshalerUtil.sizeRepeatedMessage(
            ExportTraceServiceRequest.RESOURCE_SPANS, resourceSpansMarshalers));
    this.resourceSpansMarshalers = resourceSpansMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(
        ExportTraceServiceRequest.RESOURCE_SPANS, resourceSpansMarshalers);
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    return MarshalerUtil.groupByResourceAndLibrary(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationLibraryInfo,
        data -> SpanMarshaler.create(data));
  }
}
