/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.trace.v1.internal.ScopeSpans;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeSpansMarshaler extends MarshalerWithSize {
  private final InstrumentationScopeMarshaler instrumentationScope;
  private final List<SpanMarshaler> spanMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationScopeSpansMarshaler(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<SpanMarshaler> spanMarshalers) {
    super(calculateSize(instrumentationScope, schemaUrlUtf8, spanMarshalers));
    this.instrumentationScope = instrumentationScope;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.spanMarshalers = spanMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ScopeSpans.SCOPE, instrumentationScope);
    output.serializeRepeatedMessage(ScopeSpans.SPANS, spanMarshalers);
    output.serializeString(ScopeSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  public static void writeTo(
      Serializer output,
      InstrumentationScopeMarshaler instrumentationScopeMarshaler,
      List<SpanData> spans,
      byte[] schemaUrlUtf8,
      MarshalerContext context)
      throws IOException {
    output.serializeMessage(ScopeSpans.SCOPE, instrumentationScopeMarshaler);
    output.serializeRepeatedMessage(ScopeSpans.SPANS, spans, SpanMarshaler::writeTo, context);
    output.serializeString(ScopeSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<SpanMarshaler> spanMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeSpans.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeBytes(ScopeSpans.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(ScopeSpans.SPANS, spanMarshalers);

    return size;
  }

  public static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<SpanData> spans,
      MarshalerContext context) {
    int sizeIndex = context.addSize();
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeSpans.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeBytes(ScopeSpans.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ScopeSpans.SPANS, spans, SpanMarshaler::calculateSize, context);
    context.setSize(sizeIndex, size);

    return size;
  }
}
