/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaller;
import io.opentelemetry.proto.trace.v1.internal.InstrumentationLibrarySpans;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeSpansMarshaler extends MarshalerWithSize {
  private final InstrumentationScopeMarshaller instrumentationScope;
  private final List<SpanMarshaler> spanMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationScopeSpansMarshaler(
      InstrumentationScopeMarshaller instrumentationScope,
      byte[] schemaUrlUtf8,
      List<SpanMarshaler> spanMarshalers) {
    super(calculateSize(instrumentationScope, schemaUrlUtf8, spanMarshalers));
    this.instrumentationScope = instrumentationScope;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.spanMarshalers = spanMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(
        InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY, instrumentationScope);
    output.serializeRepeatedMessage(InstrumentationLibrarySpans.SPANS, spanMarshalers);
    output.serializeString(InstrumentationLibrarySpans.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationScopeMarshaller instrumentationScope,
      byte[] schemaUrlUtf8,
      List<SpanMarshaler> spanMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeMessage(
            InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY, instrumentationScope);
    size += MarshalerUtil.sizeBytes(InstrumentationLibrarySpans.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(InstrumentationLibrarySpans.SPANS, spanMarshalers);
    return size;
  }
}
