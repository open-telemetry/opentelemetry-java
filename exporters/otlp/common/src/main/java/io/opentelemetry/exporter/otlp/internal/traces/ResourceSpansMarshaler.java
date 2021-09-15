/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ResourceMarshaler;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceSpans.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceSpansMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrlUtf8;
  private final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers;

  /** Returns Marshalers of ResourceSpans created by grouping the provided SpanData. */
  public static ResourceSpansMarshaler[] create(Collection<SpanData> spanDataList) {
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
    return resourceSpansMarshalers;
  }

  ResourceSpansMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrlUtf8,
      InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrlUtf8, instrumentationLibrarySpansMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.instrumentationLibrarySpansMarshalers = instrumentationLibrarySpansMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceSpans.INSTRUMENTATION_LIBRARY_SPANS, instrumentationLibrarySpansMarshalers);
    output.serializeString(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrlUtf8,
      InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceSpans.INSTRUMENTATION_LIBRARY_SPANS, instrumentationLibrarySpansMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<SpanMarshaler>>>
      groupByResourceAndLibrary(Collection<SpanData> spanDataList) {
    return MarshalerUtil.groupByResourceAndLibrary(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationLibraryInfo,
        SpanMarshaler::create);
  }
}
