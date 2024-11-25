/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1development.internal.AggregationTemporality;
import io.opentelemetry.proto.profiles.v1development.internal.ValueType;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class ValueTypeMarshaler extends MarshalerWithSize {

  private static final ValueTypeMarshaler[] EMPTY_REPEATED = new ValueTypeMarshaler[0];

  private final int typeStrindex;
  private final int unitStrindex;
  private final ProtoEnumInfo aggregationTemporality;

  static ValueTypeMarshaler create(ValueTypeData valueTypeData) {
    ProtoEnumInfo aggregationTemporality =
        AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;
    if (valueTypeData.getAggregationTemporality() != null) {
      switch (valueTypeData.getAggregationTemporality()) {
        case DELTA:
          aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
          break;
        case CUMULATIVE:
          aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
          break;
      }
    }
    return new ValueTypeMarshaler(
        valueTypeData.getTypeStrindex(), valueTypeData.getUnitStrindex(), aggregationTemporality);
  }

  static ValueTypeMarshaler[] createRepeated(List<ValueTypeData> items) {
    if (items.isEmpty()) {
      return EMPTY_REPEATED;
    }

    ValueTypeMarshaler[] valueTypeMarshalers = new ValueTypeMarshaler[items.size()];
    items.forEach(
        item ->
            new Consumer<ValueTypeData>() {
              int index = 0;

              @Override
              public void accept(ValueTypeData valueTypeData) {
                valueTypeMarshalers[index++] = ValueTypeMarshaler.create(valueTypeData);
              }
            });
    return valueTypeMarshalers;
  }

  private ValueTypeMarshaler(
      int typeStrindex, int unitStrindex, ProtoEnumInfo aggregationTemporality) {
    super(calculateSize(typeStrindex, unitStrindex, aggregationTemporality));
    this.typeStrindex = typeStrindex;
    this.unitStrindex = unitStrindex;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(ValueType.TYPE_STRINDEX, typeStrindex);
    output.serializeInt64(ValueType.UNIT_STRINDEX, unitStrindex);
    output.serializeEnum(ValueType.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  private static int calculateSize(
      int typeStrindex, int unitStrindex, ProtoEnumInfo aggregationTemporality) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt32(ValueType.TYPE_STRINDEX, typeStrindex);
    size += MarshalerUtil.sizeInt32(ValueType.UNIT_STRINDEX, unitStrindex);
    size += MarshalerUtil.sizeEnum(ValueType.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }
}
