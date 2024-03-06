/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.ProtoFieldInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Metric;
import java.io.IOException;

abstract class MetricMarshaler extends Marshaler {

  abstract protected String getName();
  abstract protected String getDescription();
  abstract protected String getUnit();
  abstract protected Marshaler getDataMarshaler();
  abstract protected ProtoFieldInfo getDataField();

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeString(Metric.NAME, getName());
    output.serializeString(Metric.DESCRIPTION, getDescription());
    output.serializeString(Metric.UNIT, getName());
    output.serializeMessage(getDataField(), getDataMarshaler());
  }

  protected static int calculateSize(
      String name,
      String description,
      String unit,
      Marshaler dataMarshaler,
      ProtoFieldInfo dataField) {
    int size = 0;
    size += MarshalerUtil.sizeStringUtf8(Metric.NAME, name);
    size += MarshalerUtil.sizeStringUtf8(Metric.DESCRIPTION, description);
    size += MarshalerUtil.sizeStringUtf8(Metric.UNIT, unit);
    size += MarshalerUtil.sizeMessage(dataField, dataMarshaler);
    return size;
  }
}
