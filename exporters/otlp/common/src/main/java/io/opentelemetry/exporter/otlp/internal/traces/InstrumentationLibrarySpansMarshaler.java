/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.traces;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.trace.v1.internal.InstrumentationLibrarySpans;
import java.io.IOException;
import java.util.List;

final class InstrumentationLibrarySpansMarshaler extends MarshalerWithSize {
  private final InstrumentationLibraryMarshaler instrumentationLibrary;
  private final List<SpanMarshaler> spanMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationLibrarySpansMarshaler(
      InstrumentationLibraryMarshaler instrumentationLibrary,
      byte[] schemaUrlUtf8,
      List<SpanMarshaler> spanMarshalers) {
    super(calculateSize(instrumentationLibrary, schemaUrlUtf8, spanMarshalers));
    this.instrumentationLibrary = instrumentationLibrary;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.spanMarshalers = spanMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(
        InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
    output.serializeRepeatedMessage(InstrumentationLibrarySpans.SPANS, spanMarshalers);
    output.serializeString(InstrumentationLibrarySpans.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationLibraryMarshaler instrumentationLibrary,
      byte[] schemaUrlUtf8,
      List<SpanMarshaler> spanMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeMessage(
            InstrumentationLibrarySpans.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
    size += MarshalerUtil.sizeBytes(InstrumentationLibrarySpans.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(InstrumentationLibrarySpans.SPANS, spanMarshalers);
    return size;
  }
}
