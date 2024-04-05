/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.Histogram;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import java.io.IOException;

final class HistogramStatelessMarshaler implements StatelessMarshaler<HistogramData> {
  static final HistogramStatelessMarshaler INSTANCE = new HistogramStatelessMarshaler();
  private static final Object DATA_POINT_SIZE_CALCULATOR_KEY = new Object();
  private static final Object DATA_POINT_WRITER_KEY = new Object();

  @Override
  public void writeTo(Serializer output, HistogramData histogram, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        Histogram.DATA_POINTS,
        histogram.getPoints(),
        HistogramDataPointStatelessMarshaler.INSTANCE,
        context,
        DATA_POINT_WRITER_KEY);
    output.serializeEnum(
        Histogram.AGGREGATION_TEMPORALITY,
        MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  @Override
  public int getBinarySerializedSize(HistogramData histogram, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Histogram.DATA_POINTS,
            histogram.getPoints(),
            HistogramDataPointStatelessMarshaler.INSTANCE,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    size +=
        MarshalerUtil.sizeEnum(
            Histogram.AGGREGATION_TEMPORALITY,
            MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
    return size;
  }
}
