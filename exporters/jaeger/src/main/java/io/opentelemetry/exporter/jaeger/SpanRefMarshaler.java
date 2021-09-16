/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.exporter.jaeger.proto.api_v2.internal.SpanRef;
import io.opentelemetry.exporter.jaeger.proto.api_v2.internal.SpanRefType;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.sdk.trace.data.LinkData;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

final class SpanRefMarshaler extends MarshalerWithSize {

  static List<SpanRefMarshaler> createRepeated(List<LinkData> links) {
    List<SpanRefMarshaler> marshalers = new ArrayList<>(links.size());
    for (LinkData link : links) {
      // we can assume that all links are *follows from*
      // https://github.com/open-telemetry/opentelemetry-java/issues/475
      // https://github.com/open-telemetry/opentelemetry-java/pull/481/files#r312577862
      marshalers.add(create(link));
      ;
    }
    return marshalers;
  }

  static SpanRefMarshaler create(SpanContext spanContext) {
    return new SpanRefMarshaler(
        spanContext.getTraceId(), spanContext.getSpanId(), SpanRefType.CHILD_OF);
  }

  static SpanRefMarshaler create(LinkData link) {
    return new SpanRefMarshaler(
        link.getSpanContext().getTraceId(),
        link.getSpanContext().getSpanId(),
        SpanRefType.FOLLOWS_FROM);
  }

  private final String traceId;
  private final String spanId;
  private final ProtoEnumInfo refType;

  SpanRefMarshaler(String traceId, String spanId, ProtoEnumInfo refType) {
    super(calculateSize(traceId, spanId, refType));
    this.traceId = traceId;
    this.spanId = spanId;
    this.refType = refType;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeTraceId(SpanRef.TRACE_ID, traceId);
    output.serializeSpanId(SpanRef.SPAN_ID, spanId);
    output.serializeEnum(SpanRef.REF_TYPE, refType);
  }

  private static int calculateSize(String traceId, String spanId, ProtoEnumInfo refType) {
    int size = 0;
    size += MarshalerUtil.sizeTraceId(SpanRef.TRACE_ID, traceId);
    size += MarshalerUtil.sizeSpanId(SpanRef.SPAN_ID, spanId);
    size += MarshalerUtil.sizeEnum(SpanRef.REF_TYPE, refType);
    return size;
  }
}
