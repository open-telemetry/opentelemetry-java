package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.otlp.ImmutableKeyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.ArrayList;
import java.util.List;

 final class ImmutableExemplarMarshaler extends ExemplarMarshaler {
  private final long timeUnixNano;
  private final ExemplarData value;
  private final ProtoFieldInfo valueField;
  private final SpanContext spanContext;
  private final List<KeyValueMarshaler> filteredAttributeMarshalers;
  private final int size;

  static List<ExemplarMarshaler> createRepeated(List<? extends ExemplarData> exemplars) {
    int numExemplars = exemplars.size();
    List<ExemplarMarshaler> marshalers = new ArrayList<>(numExemplars);
    for (int i = 0; i < numExemplars; i++) {
      marshalers.add(ImmutableExemplarMarshaler.create(exemplars.get(i)));
    }
    return marshalers;
  }

  private static ExemplarMarshaler create(ExemplarData exemplar) {
    List<KeyValueMarshaler> attributeMarshalers =
        ImmutableKeyValueMarshaler.createForAttributes(exemplar.getFilteredAttributes());

    ProtoFieldInfo valueField;
    if (exemplar instanceof LongExemplarData) {
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT;
    } else {
      assert exemplar instanceof DoubleExemplarData;
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_DOUBLE;
    }

    return new ImmutableExemplarMarshaler(
        exemplar.getEpochNanos(),
        exemplar,
        valueField,
        exemplar.getSpanContext(),
        attributeMarshalers);
  }

  private ImmutableExemplarMarshaler(
      long timeUnixNano,
      ExemplarData value,
      ProtoFieldInfo valueField,
      SpanContext spanContext,
      List<KeyValueMarshaler> filteredAttributeMarshalers) {
    this.timeUnixNano = timeUnixNano;
    this.value = value;
    this.valueField = valueField;
    this.spanContext = spanContext;
    this.filteredAttributeMarshalers = filteredAttributeMarshalers;
    this.size = calculateSize(
        timeUnixNano, valueField, value, spanContext, filteredAttributeMarshalers);
  }

   @Override
   public int getBinarySerializedSize() {
     return size;
   }

   @Override
   long getTimeUnixNano() {
     return timeUnixNano;
   }

   @Override
   ExemplarData getValue() {
     return value;
   }

   @Override
   ProtoFieldInfo getValueField() {
     return valueField;
   }

   @Override
   SpanContext getSpanContext() {
     return spanContext;
   }

   @Override
   List<KeyValueMarshaler> getFilteredAttributes() {
     return filteredAttributeMarshalers;
   }
 }
