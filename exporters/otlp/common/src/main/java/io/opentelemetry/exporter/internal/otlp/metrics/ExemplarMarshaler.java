/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.Exemplar;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.io.IOException;
import java.util.List;

final class ExemplarMarshaler extends MarshalerWithSize {

  private final long timeUnixNano;

  private final ExemplarData value;
  private final ProtoFieldInfo valueField;

  private final SpanContext spanContext;

  private final KeyValueMarshaler[] filteredAttributeMarshalers;

  static ExemplarMarshaler[] createRepeated(List<? extends ExemplarData> exemplars) {
    int numExemplars = exemplars.size();
    ExemplarMarshaler[] marshalers = new ExemplarMarshaler[numExemplars];
    for (int i = 0; i < numExemplars; i++) {
      marshalers[i] = ExemplarMarshaler.create(exemplars.get(i));
    }
    return marshalers;
  }

  // Visible for testing
  static ExemplarMarshaler create(ExemplarData exemplar) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(exemplar.getFilteredAttributes());

    return new ExemplarMarshaler(
        exemplar.getEpochNanos(),
        exemplar,
        toProtoExemplarValueType(exemplar),
        exemplar.getSpanContext(),
        attributeMarshalers);
  }

  private ExemplarMarshaler(
      long timeUnixNano,
      ExemplarData value,
      ProtoFieldInfo valueField,
      SpanContext spanContext,
      KeyValueMarshaler[] filteredAttributeMarshalers) {
    super(calculateSize(timeUnixNano, valueField, value, spanContext, filteredAttributeMarshalers));
    this.timeUnixNano = timeUnixNano;
    this.value = value;
    this.valueField = valueField;
    this.spanContext = spanContext;
    this.filteredAttributeMarshalers = filteredAttributeMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(Exemplar.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == Exemplar.AS_INT) {
      output.serializeFixed64Optional(valueField, ((LongExemplarData) value).getValue());
    } else {
      output.serializeDoubleOptional(valueField, ((DoubleExemplarData) value).getValue());
    }
    if (spanContext.isValid()) {
      output.serializeSpanId(Exemplar.SPAN_ID, spanContext.getSpanId());
      output.serializeTraceId(Exemplar.TRACE_ID, spanContext.getTraceId());
    }
    output.serializeRepeatedMessage(Exemplar.FILTERED_ATTRIBUTES, filteredAttributeMarshalers);
  }

  private static int calculateSize(
      long timeUnixNano,
      ProtoFieldInfo valueField,
      ExemplarData value,
      SpanContext spanContext,
      KeyValueMarshaler[] filteredAttributeMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(Exemplar.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == Exemplar.AS_INT) {
      size += MarshalerUtil.sizeFixed64Optional(valueField, ((LongExemplarData) value).getValue());
    } else {
      size += MarshalerUtil.sizeDoubleOptional(valueField, ((DoubleExemplarData) value).getValue());
    }
    if (spanContext.isValid()) {
      size += MarshalerUtil.sizeSpanId(Exemplar.SPAN_ID, spanContext.getSpanId());
      size += MarshalerUtil.sizeTraceId(Exemplar.TRACE_ID, spanContext.getTraceId());
    }
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Exemplar.FILTERED_ATTRIBUTES, filteredAttributeMarshalers);
    return size;
  }

  static ProtoFieldInfo toProtoExemplarValueType(ExemplarData exemplar) {
    if (exemplar instanceof LongExemplarData) {
      return Exemplar.AS_INT;
    } else {
      assert exemplar instanceof DoubleExemplarData;
      return Exemplar.AS_DOUBLE;
    }
  }
}
