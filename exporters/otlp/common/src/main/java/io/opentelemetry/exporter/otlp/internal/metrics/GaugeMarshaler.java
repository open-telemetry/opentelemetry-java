/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Gauge;
import io.opentelemetry.sdk.metrics.data.GaugeData;
import io.opentelemetry.sdk.metrics.data.PointData;
import java.io.IOException;

final class GaugeMarshaler extends MarshalerWithSize {
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

  private static int calculateSize(NumberDataPointMarshaler[] dataPoints) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Gauge.DATA_POINTS, dataPoints);
    return size;
  }
}
