package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.exporter.internal.otlp.MarshallerObjectPools;
import io.opentelemetry.exporter.internal.otlp.MutableKeyValueMarshaler;
import io.opentelemetry.sdk.internal.DynamicList;
import io.opentelemetry.sdk.metrics.data.DoubleExemplarData;
import io.opentelemetry.sdk.metrics.data.ExemplarData;
import io.opentelemetry.sdk.metrics.data.LongExemplarData;
import java.util.List;

public final class MutableExemplarMarshaler extends ExemplarMarshaler {
  private long timeUnixNano;
  private ExemplarData value;
  private ProtoFieldInfo valueField;
  private SpanContext spanContext;
  private DynamicList<KeyValueMarshaler> filteredAttributeMarshalers = DynamicList.empty();
  private int size;

  static void createRepeatedIntoDynamicList(
      List<? extends ExemplarData> exemplars,
      DynamicList<ExemplarMarshaler> exemplarMarshalers,
      MarshallerObjectPools marshallerObjectPools) {
    int numExemplars = exemplars.size();
    exemplarMarshalers.resizeAndClear(numExemplars);
    for (int i = 0; i < numExemplars; i++) {
      exemplarMarshalers.add(create(exemplars.get(i), marshallerObjectPools));
    }
  }

  private static ExemplarMarshaler create(
      ExemplarData exemplar,
      MarshallerObjectPools marshallerObjectPools) {

    MutableExemplarMarshaler mutableExemplarMarshaler =
        marshallerObjectPools.getMutableExemplarMarshallerPool().borrowObject();

    DynamicList<KeyValueMarshaler> attributeMarshallersDynamicList =
        mutableExemplarMarshaler.filteredAttributeMarshalers;

    MutableKeyValueMarshaler.createForAttributesIntoDynamicList(
            exemplar.getFilteredAttributes(),
            attributeMarshallersDynamicList,
            marshallerObjectPools);

    ProtoFieldInfo valueField;
    if (exemplar instanceof LongExemplarData) {
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_INT;
    } else {
      assert exemplar instanceof DoubleExemplarData;
      valueField = io.opentelemetry.proto.metrics.v1.internal.Exemplar.AS_DOUBLE;
    }

    mutableExemplarMarshaler.set(
        exemplar.getEpochNanos(),
        exemplar,
        valueField,
        exemplar.getSpanContext(),
        attributeMarshallersDynamicList);

    return mutableExemplarMarshaler;
  }

  private void set(
      long timeUnixNano,
      ExemplarData value,
      ProtoFieldInfo valueField,
      SpanContext spanContext,
      DynamicList<KeyValueMarshaler> filteredAttributeMarshalers) {
    this.timeUnixNano = timeUnixNano;
    this.value = value;
    this.valueField = valueField;
    this.spanContext = spanContext;
    this.filteredAttributeMarshalers = filteredAttributeMarshalers;
    this.size = calculateSize(
        timeUnixNano,
        valueField,
        value,
        spanContext,
        filteredAttributeMarshalers);
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
