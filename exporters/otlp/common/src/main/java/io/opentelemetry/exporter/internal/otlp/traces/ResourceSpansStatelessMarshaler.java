/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.trace.v1.internal.ResourceSpans;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceSpans.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceSpansStatelessMarshaler
    implements StatelessMarshaler2<Resource, Map<InstrumentationScopeInfo, List<SpanData>>> {
  static final ResourceSpansStatelessMarshaler INSTANCE = new ResourceSpansStatelessMarshaler();
  private static final MarshalerContext.Key SCOPE_SPAN_WRITER_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key SCOPE_SPAN_SIZE_CALCULATOR_KEY = MarshalerContext.key();

  @Override
  public void writeTo(
      Serializer output,
      Resource resource,
      Map<InstrumentationScopeInfo, List<SpanData>> scopeMap,
      MarshalerContext context)
      throws IOException {
    ResourceMarshaler resourceMarshaler = context.getData(ResourceMarshaler.class);
    output.serializeMessage(ResourceSpans.RESOURCE, resourceMarshaler);

    output.serializeRepeatedMessage(
        ResourceSpans.SCOPE_SPANS,
        scopeMap,
        InstrumentationScopeSpansStatelessMarshaler.INSTANCE,
        context,
        SCOPE_SPAN_WRITER_KEY);

    output.serializeString(ResourceSpans.SCHEMA_URL, resource.getSchemaUrl(), context);
  }

  @Override
  public int getBinarySerializedSize(
      Resource resource,
      Map<InstrumentationScopeInfo, List<SpanData>> scopeMap,
      MarshalerContext context) {

    int size = 0;

    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    context.addData(resourceMarshaler);
    size += MarshalerUtil.sizeMessage(ResourceSpans.RESOURCE, resourceMarshaler);

    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceSpans.SCOPE_SPANS,
            scopeMap,
            InstrumentationScopeSpansStatelessMarshaler.INSTANCE,
            context,
            SCOPE_SPAN_SIZE_CALCULATOR_KEY);

    size += MarshalerUtil.sizeString(ResourceSpans.SCHEMA_URL, resource.getSchemaUrl(), context);

    return size;
  }
}
