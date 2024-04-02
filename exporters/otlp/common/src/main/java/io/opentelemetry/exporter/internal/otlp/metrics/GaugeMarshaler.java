/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Gauge;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.io.IOException;

final class GaugeMarshaler extends MarshalerWithSize {
  private static final Object DATA_POINT_SIZE_CALCULATOR_KEY = new Object();
  private static final Object DATA_POINT_WRITER_KEY = new Object();

  private final NumberDataPointMarshaler[] dataPoints;

  static GaugeMarshaler create(GaugeData<? extends PointData> gauge) {
    NumberDataPointMarshaler[] dataPointMarshalers =
        NumberDataPointMarshaler.createRepeated(gauge.getPoints());

    return new GaugeMarshaler(dataPointMarshalers);
  }

  private GaugeMarshaler(NumberDataPointMarshaler[] dataPoints) {
    super(calculateSize(dataPoints));
    this.dataPoints = dataPoints;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Gauge.DATA_POINTS, dataPoints);
  }

  public static void writeTo(
      Serializer output, GaugeData<? extends PointData> gauge, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        Gauge.DATA_POINTS,
        gauge.getPoints(),
        NumberDataPointMarshaler::writeTo,
        context,
        DATA_POINT_WRITER_KEY);
  }

  private static int calculateSize(NumberDataPointMarshaler[] dataPoints) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Gauge.DATA_POINTS, dataPoints);
    return size;
  }

  public static int calculateSize(GaugeData<? extends PointData> gauge, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Gauge.DATA_POINTS,
            gauge.getPoints(),
            NumberDataPointMarshaler::calculateSize,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    return size;
  }
}
