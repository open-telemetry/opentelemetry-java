/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class NumberDataPointMarshaler extends Marshaler {

  protected NumberDataPointMarshaler() {
    super();
  }

  protected abstract long getStartTimeUnixNano();
  protected abstract long getTimeUnixNano();
  protected abstract PointData getValue();
  protected abstract ProtoFieldInfo getValueField();
  protected abstract List<ExemplarMarshaler> getExemplars();
  protected abstract List<KeyValueMarshaler> getAttributes();

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(NumberDataPoint.START_TIME_UNIX_NANO, getStartTimeUnixNano());
    output.serializeFixed64(NumberDataPoint.TIME_UNIX_NANO, getTimeUnixNano());
    if (getValueField() == NumberDataPoint.AS_INT) {
      output.serializeFixed64Optional(getValueField(), ((LongPointData) getValue()).getValue());
    } else {
      output.serializeDoubleOptional(getValueField(), ((DoublePointData) getValue()).getValue());
    }
    output.serializeRepeatedMessage(NumberDataPoint.EXEMPLARS, getExemplars());
    output.serializeRepeatedMessage(NumberDataPoint.ATTRIBUTES, getAttributes());
  }

  protected static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      ProtoFieldInfo valueField,
      PointData value,
      List<ExemplarMarshaler> exemplars,
      List<KeyValueMarshaler> attributes) {
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
