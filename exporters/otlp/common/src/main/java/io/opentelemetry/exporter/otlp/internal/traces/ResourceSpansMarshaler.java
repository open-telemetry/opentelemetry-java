/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ResourceMarshaler;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.ResourceSpans;
import java.io.IOException;

final class ResourceSpansMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrlUtf8;
  private final InstrumentationLibrarySpansMarshaler[] instrumentationLibrarySpansMarshalers;

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
}
