/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.Gauge;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.io.IOException;

final class GaugeStatelessMarshaler implements StatelessMarshaler<GaugeData<? extends PointData>> {
  static final GaugeStatelessMarshaler INSTANCE = new GaugeStatelessMarshaler();
  private static final Object DATA_POINT_SIZE_CALCULATOR_KEY = new Object();
  private static final Object DATA_POINT_WRITER_KEY = new Object();

  @Override
  public void writeTo(
      Serializer output, GaugeData<? extends PointData> gauge, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        Gauge.DATA_POINTS,
        gauge.getPoints(),
        NumberDataPointStatelessMarshaler.INSTANCE,
        context,
        DATA_POINT_WRITER_KEY);
  }

  @Override
  public int getBinarySerializedSize(
      GaugeData<? extends PointData> gauge, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Gauge.DATA_POINTS,
            gauge.getPoints(),
            NumberDataPointStatelessMarshaler.INSTANCE,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    return size;
  }
}
