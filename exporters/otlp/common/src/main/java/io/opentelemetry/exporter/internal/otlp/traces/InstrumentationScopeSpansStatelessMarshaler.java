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
        context.getData(InstrumentationScopeMarshaler.class);

    output.serializeMessage(ScopeSpans.SCOPE, instrumentationScopeMarshaler);
    output.serializeRepeatedMessage(
        ScopeSpans.SPANS, spans, SpanStatelessMarshaler.INSTANCE, context);
    output.serializeString(ScopeSpans.SCHEMA_URL, instrumentationScope.getSchemaUrl(), context);
  }

  @Override
  public int getBinarySerializedSize(
      InstrumentationScopeInfo instrumentationScope,
      List<SpanData> spans,
      MarshalerContext context) {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        InstrumentationScopeMarshaler.create(instrumentationScope);
    context.addData(instrumentationScopeMarshaler);

    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeSpans.SCOPE, instrumentationScopeMarshaler);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ScopeSpans.SPANS, spans, SpanStatelessMarshaler.INSTANCE, context);
    size +=
        MarshalerUtil.sizeString(
            ScopeSpans.SCHEMA_URL, instrumentationScope.getSchemaUrl(), context);

    return size;
  }
}
