/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoFieldInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.sdk.metrics.data.DoubleExemplar;
import io.opentelemetry.sdk.metrics.data.Exemplar;
import io.opentelemetry.sdk.metrics.data.LongExemplar;
import java.io.IOException;
import java.util.List;
import javax.annotation.Nullable;

final class ExemplarMarshaler extends MarshalerWithSize {

  private final long timeUnixNano;

  private final Exemplar value;
  private final ProtoFieldInfo valueField;

  @Nullable private final String spanId;
  @Nullable private final String traceId;

  private final KeyValueMarshaler[] filteredAttributeMarshalers;

  static ExemplarMarshaler[] createRepeated(List<Exemplar> exemplars) {
    int numExemplars = exemplars.size();
    ExemplarMarshaler[] marshalers = new ExemplarMarshaler[numExemplars];
    for (int i = 0; i < numExemplars; i++) {
      marshalers[i] = ExemplarMarshaler.create(exemplars.get(i));
    }
    return marshalers;
  }

  private static ExemplarMarshaler create(Exemplar exemplar) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(exemplar.getFilteredAttributes());

    final ProtoFieldInfo valueField;
    if (exemplar instanceof LongExemplar) {
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT;
    } else {
      assert exemplar instanceof DoubleExemplar;
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_DOUBLE;
    }

    return new ExemplarMarshaler(
        exemplar.getEpochNanos(),
        exemplar,
        valueField,
        exemplar.getSpanId(),
        exemplar.getTraceId(),
        attributeMarshalers);
  }

  private ExemplarMarshaler(
      long timeUnixNano,
      Exemplar value,
      ProtoFieldInfo valueField,
      @Nullable String spanId,
      @Nullable String traceId,
      KeyValueMarshaler[] filteredAttributeMarshalers) {
    super(
        calculateSize(
            timeUnixNano, valueField, value, spanId, traceId, filteredAttributeMarshalers));
    this.timeUnixNano = timeUnixNano;
    this.value = value;
    this.valueField = valueField;
    this.spanId = spanId;
    this.traceId = traceId;
    this.filteredAttributeMarshalers = filteredAttributeMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      output.serializeFixed64(valueField, ((LongExemplar) value).getValue());
    } else {
      output.serializeDouble(valueField, ((DoubleExemplar) value).getValue());
    }
    output.serializeSpanId(io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID, spanId);
    output.serializeTraceId(io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID, traceId);
    output.serializeRepeatedMessage(
        io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
        filteredAttributeMarshalers);
  }

  private static int calculateSize(
      long timeUnixNano,
      ProtoFieldInfo valueField,
      Exemplar value,
      @Nullable String spanId,
      @Nullable String traceId,
      KeyValueMarshaler[] filteredAttributeMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT) {
      size += MarshalerUtil.sizeFixed64(valueField, ((LongExemplar) value).getValue());
    } else {
      size += MarshalerUtil.sizeDouble(valueField, ((DoubleExemplar) value).getValue());
    }
    size +=
        MarshalerUtil.sizeSpanId(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.SPAN_ID, spanId);
    size +=
        MarshalerUtil.sizeTraceId(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.TRACE_ID, traceId);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            io.opentelemetry.proto.metrics.v1.internal.Exemplar.FILTERED_ATTRIBUTES,
            filteredAttributeMarshalers);
    return size;
  }
}
