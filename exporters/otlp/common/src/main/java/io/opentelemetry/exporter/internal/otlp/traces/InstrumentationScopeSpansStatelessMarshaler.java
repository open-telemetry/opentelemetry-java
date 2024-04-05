/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.trace.v1.internal.ScopeSpans;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeSpansStatelessMarshaler
    implements StatelessMarshaler2<InstrumentationScopeInfo, List<SpanData>> {
  static final InstrumentationScopeSpansStatelessMarshaler INSTANCE =
      new InstrumentationScopeSpansStatelessMarshaler();

  @Override
  public void writeTo(
      Serializer output,
      InstrumentationScopeInfo instrumentationScope,
      List<SpanData> spans,
      MarshalerContext context)
      throws IOException {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        context.getObject(InstrumentationScopeMarshaler.class);
    byte[] schemaUrlUtf8 = context.getByteArray();

    output.serializeMessage(ScopeSpans.SCOPE, instrumentationScopeMarshaler);
    output.serializeRepeatedMessage(
        ScopeSpans.SPANS, spans, SpanStatelessMarshaler.INSTANCE, context);
    output.serializeString(ScopeSpans.SCHEMA_URL, schemaUrlUtf8);
  }

  @Override
  public int getBinarySerializedSize(
      InstrumentationScopeInfo instrumentationScope,
      List<SpanData> spans,
      MarshalerContext context) {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        InstrumentationScopeMarshaler.create(instrumentationScope);
    context.addData(instrumentationScopeMarshaler);
    // XXX
    byte[] schemaUrlUtf8 = MarshalerUtil.toBytes(instrumentationScope.getSchemaUrl());
    context.addData(schemaUrlUtf8);

    // int sizeIndex = context.addSize();
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeSpans.SCOPE, instrumentationScopeMarshaler);
    size += MarshalerUtil.sizeBytes(ScopeSpans.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ScopeSpans.SPANS, spans, SpanStatelessMarshaler.INSTANCE, context);
    // context.setSize(sizeIndex, size);

    return size;
  }
}
