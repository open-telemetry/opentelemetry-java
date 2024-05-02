/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import static io.opentelemetry.exporter.internal.otlp.metrics.NumberDataPointMarshaler.toProtoPointValueType;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.NumberDataPoint;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.io.IOException;

/** See {@link NumberDataPointMarshaler}. */
final class NumberDataPointStatelessMarshaler implements StatelessMarshaler<PointData> {
  static final NumberDataPointStatelessMarshaler INSTANCE = new NumberDataPointStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, PointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(NumberDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    ProtoFieldInfo valueField = toProtoPointValueType(point);
    if (valueField == NumberDataPoint.AS_INT) {
      output.serializeFixed64Optional(valueField, ((LongPointData) point).getValue());
    } else {
      output.serializeDoubleOptional(valueField, ((DoublePointData) point).getValue());
    }
    output.serializeRepeatedMessageWithContext(
        NumberDataPoint.EXEMPLARS,
        point.getExemplars(),
        ExemplarStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessageWithContext(
        NumberDataPoint.ATTRIBUTES,
        point.getAttributes(),
        KeyValueStatelessMarshaler.INSTANCE,
        context);
  }

  @Override
  public int getBinarySerializedSize(PointData point, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(NumberDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    ProtoFieldInfo valueField = toProtoPointValueType(point);
    if (valueField == NumberDataPoint.AS_INT) {
      size += MarshalerUtil.sizeFixed64Optional(valueField, ((LongPointData) point).getValue());
    } else {
      size += MarshalerUtil.sizeDoubleOptional(valueField, ((DoublePointData) point).getValue());
    }
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            NumberDataPoint.EXEMPLARS,
            point.getExemplars(),
            ExemplarStatelessMarshaler.INSTANCE,
            context);
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            NumberDataPoint.ATTRIBUTES,
            point.getAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);
    return size;
  }
}
