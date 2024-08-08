/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.profiles;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.profiles.v1experimental.internal.AggregationTemporality;
import io.opentelemetry.proto.profiles.v1experimental.internal.ValueType;
import java.io.IOException;
import java.util.List;
import java.util.function.Consumer;

final class ValueTypeMarshaler extends MarshalerWithSize {

  private static final ValueTypeMarshaler[] EMPTY_REPEATED = new ValueTypeMarshaler[0];

  private final long type;
  private final long unit;
  private final ProtoEnumInfo aggregationTemporality;

  static ValueTypeMarshaler create(ValueTypeData valueTypeData) {
    ProtoEnumInfo aggregationTemporality =
        AggregationTemporality.AGGREGATION_TEMPORALITY_UNSPECIFIED;
    if (valueTypeData.aggregationTemporality() != null) {
      switch (valueTypeData.aggregationTemporality()) {
        case DELTA:
          aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_DELTA;
          break;
        case CUMULATIVE:
          aggregationTemporality = AggregationTemporality.AGGREGATION_TEMPORALITY_CUMULATIVE;
          break;
      }
    }
    return new ValueTypeMarshaler(
        valueTypeData.type(), valueTypeData.unit(), aggregationTemporality);
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

  private ValueTypeMarshaler(long type, long unit, ProtoEnumInfo aggregationTemporality) {
    super(calculateSize(type, unit, aggregationTemporality));
    this.type = type;
    this.unit = unit;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeInt64(ValueType.TYPE, type);
    output.serializeInt64(ValueType.UNIT, unit);
    output.serializeEnum(ValueType.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  private static int calculateSize(long type, long unit, ProtoEnumInfo aggregationTemporality) {
    int size;
    size = 0;
    size += MarshalerUtil.sizeInt64(ValueType.TYPE, type);
    size += MarshalerUtil.sizeInt64(ValueType.UNIT, unit);
    size += MarshalerUtil.sizeEnum(ValueType.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }
}
