/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static io.opentelemetry.exporter.internal.otlp.metrics.ExemplarMarshaler.getValueField;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.io.IOException;

final class ExemplarStatelessMarshaler implements StatelessMarshaler<ExemplarData> {
  static final ExemplarStatelessMarshaler INSTANCE = new ExemplarStatelessMarshaler();

  private ExemplarStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, ExemplarData exemplar, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO,
        exemplar.getEpochNanos());
    ProtoFieldInfo valueField = getValueField(exemplar);
    if (valueField == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      output.serializeFixed64Optional(valueField, ((LongExemplarData) exemplar).getValue());
    } else {
      output.serializeDoubleOptional(valueField, ((DoubleExemplarData) exemplar).getValue());
    }
    SpanContext spanContext = exemplar.getSpanContext();
    if (spanContext.isValid()) {
      output.serializeSpanId(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID,
          spanContext.getSpanId(),
          context);
      output.serializeTraceId(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID,
          spanContext.getTraceId(),
          context);
    }
    output.serializeRepeatedMessage(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
        exemplar.getFilteredAttributes(),
        KeyValueStatelessMarshaler.INSTANCE,
        context);
  }

  @Override
  public int getBinarySerializedSize(ExemplarData exemplar, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO,
            exemplar.getEpochNanos());
    ProtoFieldInfo valueField = getValueField(exemplar);
    if (valueField == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      size +=
          MarshalerUtil.sizeFixed64Optional(valueField, ((LongExemplarData) exemplar).getValue());
    } else {
      size +=
          MarshalerUtil.sizeDoubleOptional(valueField, ((DoubleExemplarData) exemplar).getValue());
    }
    SpanContext spanContext = exemplar.getSpanContext();
    if (spanContext.isValid()) {
      size +=
          MarshalerUtil.sizeSpanId(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID, spanContext.getSpanId());
      size +=
          MarshalerUtil.sizeTraceId(
              io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID,
              spanContext.getTraceId());
    }
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
            exemplar.getFilteredAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);

    return size;
  }
}
