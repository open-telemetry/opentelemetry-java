/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.NumberDataPoint;
import io.opentelemetry.sdk.metrics.data.DoublePointData;
import io.opentelemetry.sdk.metrics.data.LongPointData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.io.IOException;
import java.util.Collection;

final class NumberDataPointMarshaler extends MarshalerWithSize {
  private final long startTimeUnixNano;
  private final long timeUnixNano;

  private final PointData value;
  private final ProtoFieldInfo valueField;

  private final ExemplarMarshaler[] exemplars;
  private final KeyValueMarshaler[] attributes;

  static NumberDataPointMarshaler[] createRepeated(Collection<? extends PointData> points) {
    int numPoints = points.size();
    NumberDataPointMarshaler[] marshalers = new NumberDataPointMarshaler[numPoints];
    int index = 0;
    for (PointData point : points) {
      marshalers[index++] = NumberDataPointMarshaler.create(point);
    }
    return marshalers;
  }

  static NumberDataPointMarshaler create(PointData point) {
    ExemplarMarshaler[] exemplarMarshalers = ExemplarMarshaler.createRepeated(point.getExemplars());
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(point.getAttributes());

    return new NumberDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point,
        getValueField(point),
        exemplarMarshalers,
        attributeMarshalers);
  }

  private NumberDataPointMarshaler(
      long startTimeUnixNano,
      long timeUnixNano,
      PointData value,
      ProtoFieldInfo valueField,
      ExemplarMarshaler[] exemplars,
      KeyValueMarshaler[] attributes) {
    super(calculateSize(startTimeUnixNano, timeUnixNano, valueField, value, exemplars, attributes));
    this.startTimeUnixNano = startTimeUnixNano;
    this.timeUnixNano = timeUnixNano;
    this.value = value;
    this.valueField = valueField;
    this.exemplars = exemplars;
    this.attributes = attributes;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    output.serializeFixed64(NumberDataPoint.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == NumberDataPoint.AS_INT) {
      output.serializeFixed64Optional(valueField, ((LongPointData) value).getValue());
    } else {
      output.serializeDoubleOptional(valueField, ((DoublePointData) value).getValue());
    }
    output.serializeRepeatedMessage(NumberDataPoint.EXEMPLARS, exemplars);
    output.serializeRepeatedMessage(NumberDataPoint.ATTRIBUTES, attributes);
  }

  public static void writeTo(Serializer output, PointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(NumberDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    ProtoFieldInfo valueField = getValueField(point);
    if (valueField == NumberDataPoint.AS_INT) {
      output.serializeFixed64Optional(valueField, ((LongPointData) point).getValue());
    } else {
      output.serializeDoubleOptional(valueField, ((DoublePointData) point).getValue());
    }
    output.serializeRepeatedMessage(
        NumberDataPoint.EXEMPLARS, point.getExemplars(), ExemplarMarshaler::writeTo, context);
    KeyValueMarshaler.writeTo(output, context, NumberDataPoint.ATTRIBUTES, point.getAttributes());
  }

  private static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      ProtoFieldInfo valueField,
      PointData value,
      ExemplarMarshaler[] exemplars,
      KeyValueMarshaler[] attributes) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(NumberDataPoint.TIME_UNIX_NANO, timeUnixNano);
    if (valueField == NumberDataPoint.AS_INT) {
      size += MarshalerUtil.sizeFixed64Optional(valueField, ((LongPointData) value).getValue());
    } else {
      size += MarshalerUtil.sizeDoubleOptional(valueField, ((DoublePointData) value).getValue());
    }
    size += MarshalerUtil.sizeRepeatedMessage(NumberDataPoint.EXEMPLARS, exemplars);
    size += MarshalerUtil.sizeRepeatedMessage(NumberDataPoint.ATTRIBUTES, attributes);
    return size;
  }

  public static int calculateSize(PointData point, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(NumberDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    ProtoFieldInfo valueField = getValueField(point);
    if (valueField == NumberDataPoint.AS_INT) {
      size += MarshalerUtil.sizeFixed64Optional(valueField, ((LongPointData) point).getValue());
    } else {
      size += MarshalerUtil.sizeDoubleOptional(valueField, ((DoublePointData) point).getValue());
    }
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            NumberDataPoint.EXEMPLARS,
            point.getExemplars(),
            ExemplarMarshaler::calculateSize,
            context);
    size +=
        KeyValueMarshaler.calculateSize(NumberDataPoint.ATTRIBUTES, point.getAttributes(), context);
    return size;
  }

  private static ProtoFieldInfo getValueField(PointData pointData) {
    if (pointData instanceof LongPointData) {
      return NumberDataPoint.AS_INT;
    } else {
      assert pointData instanceof DoublePointData;
      return NumberDataPoint.AS_DOUBLE;
    }
  }
}
