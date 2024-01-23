/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.io.IOException;
import java.util.List;

public abstract class ExemplarMarshaler extends Marshaler {

  abstract long getTimeUnixNano();
  abstract ExemplarData getValue();
  abstract ProtoFieldInfo getValueField();
  abstract SpanContext getSpanContext();
  abstract List<KeyValueMarshaler> getFilteredAttributes();

  protected ExemplarMarshaler() {
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, getTimeUnixNano());
    if (getValueField() == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      output.serializeFixed64Optional(getValueField(), ((LongExemplarData) getValue()).getValue());
    } else {
      output.serializeDoubleOptional(getValueField(), ((DoubleExemplarData) getValue()).getValue());
    }
    if (getSpanContext().isValid()) {
      output.serializeSpanId(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID,
          getSpanContext().getSpanId());
      output.serializeTraceId(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID,
          getSpanContext().getTraceId());
    }
    output.serializeRepeatedMessage(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
        getFilteredAttributes());
  }

  protected static int calculateSize(
      long timeUnixNano,
      ProtoFieldInfo valueField,
      ExemplarData value,
      SpanContext spanContext,
      List<KeyValueMarshaler> filteredAttributeMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      size += MarshalerUtil.sizeFixed64Optional(valueField, ((LongExemplarData) value).getValue());
    } else {
      size += MarshalerUtil.sizeDoubleOptional(valueField, ((DoubleExemplarData) value).getValue());
    }
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
            filteredAttributeMarshalers);
    return size;
  }
}
