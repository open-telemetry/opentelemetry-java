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

  private static ExemplarMarshaler create(ExemplarData exemplar) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(exemplar.getFilteredAttributes());

    ProtoFieldInfo valueField;
    if (exemplar instanceof LongExemplarData) {
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT;
    } else {
      assert exemplar instanceof DoubleExemplarData;
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_DOUBLE;
    }

    return new ExemplarMarshaler(
        exemplar.getEpochNanos(),
        exemplar,
        valueField,
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
    output.serializeFixed64(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      output.serializeFixed64Optional(valueField, ((LongExemplarData) value).getValue());
    } else {
      output.serializeDoubleOptional(valueField, ((DoubleExemplarData) value).getValue());
    }
    if (spanContext.isValid()) {
      output.serializeSpanId(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID, spanContext.getSpanId());
      output.serializeTraceId(
          io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID, spanContext.getTraceId());
    }
    output.serializeRepeatedMessage(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
        filteredAttributeMarshalers);
  }

  private static int calculateSize(
      long timeUnixNano,
      ProtoFieldInfo valueField,
      ExemplarData value,
      SpanContext spanContext,
      KeyValueMarshaler[] filteredAttributeMarshalers) {
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
