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
        KeyValueMarshaler.createRepeated(point.getAttributes());

    final ProtoFieldInfo valueField;
    if (point instanceof LongPointData) {
      valueField = NumberDataPoint.AS_INT;
    } else {
      assert point instanceof DoublePointData;
      valueField = NumberDataPoint.AS_DOUBLE;
    }

    return new NumberDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point,
        valueField,
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
}
