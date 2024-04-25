/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.trace.v1.internal.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
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
  private final InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers;

  /** Returns Marshalers of ResourceSpans created by grouping the provided SpanData. */
  @SuppressWarnings("AvoidObjectArrays")
  public static ResourceSpansMarshaler[] create(Collection<SpanData> spanDataList) {
    Map<Resource, Map<InstrumentationScopeInfo, List<SpanMarshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(spanDataList);

    ResourceSpansMarshaler[] resourceSpansMarshalers =
        new ResourceSpansMarshaler[resourceAndScopeMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<SpanMarshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers =
          new InstrumentationScopeSpansMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationScopeInfo, List<SpanMarshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationScopeSpansMarshalers[posInstrumentation++] =
            new InstrumentationScopeSpansMarshaler(
                InstrumentationScopeMarshaler.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceSpansMarshalers[posResource++] =
          new ResourceSpansMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationScopeSpansMarshalers);
    }
    return resourceSpansMarshalers;
  }

  ResourceSpansMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrlUtf8,
      InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrlUtf8, instrumentationScopeSpansMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.instrumentationScopeSpansMarshalers = instrumentationScopeSpansMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(ResourceSpans.SCOPE_SPANS, instrumentationScopeSpansMarshalers);
    output.serializeString(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrlUtf8,
      InstrumentationScopeSpansMarshaler[] instrumentationScopeSpansMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceSpans.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceSpans.SCOPE_SPANS, instrumentationScopeSpansMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<SpanMarshaler>>>
      groupByResourceAndScope(Collection<SpanData> spanDataList) {
    return MarshalerUtil.groupByResourceAndScope(
        spanDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        SpanData::getResource,
        SpanData::getInstrumentationScopeInfo,
        SpanMarshaler::create);
  }
}
