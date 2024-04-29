/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.trace.v1.internal.ScopeSpans;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.List;

/** See {@link InstrumentationScopeSpansMarshaler}. */
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
    output.serializeRepeatedMessageWithContext(
        ScopeSpans.SPANS, spans, SpanStatelessMarshaler.INSTANCE, context);
    output.serializeStringWithContext(
        ScopeSpans.SCHEMA_URL, instrumentationScope.getSchemaUrl(), context);
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
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            ScopeSpans.SPANS, spans, SpanStatelessMarshaler.INSTANCE, context);
    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            ScopeSpans.SCHEMA_URL, instrumentationScope.getSchemaUrl(), context);

    return size;
  }
}
