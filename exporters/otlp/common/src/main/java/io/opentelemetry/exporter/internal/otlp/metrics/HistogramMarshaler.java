/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Histogram;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import java.io.IOException;

final class HistogramMarshaler extends MarshalerWithSize {
  private static final Object DATA_POINT_SIZE_CALCULATOR_KEY = new Object();
  private static final Object DATA_POINT_WRITER_KEY = new Object();

  private final HistogramDataPointMarshaler[] dataPoints;
  private final ProtoEnumInfo aggregationTemporality;

  static HistogramMarshaler create(HistogramData histogram) {
    HistogramDataPointMarshaler[] dataPointMarshalers =
        HistogramDataPointMarshaler.createRepeated(histogram.getPoints());
    return new HistogramMarshaler(
        dataPointMarshalers,
        MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  private HistogramMarshaler(
      HistogramDataPointMarshaler[] dataPoints, ProtoEnumInfo aggregationTemporality) {
    super(calculateSize(dataPoints, aggregationTemporality));
    this.dataPoints = dataPoints;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
    output.serializeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  public static void writeTo(Serializer output, HistogramData histogram, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        Histogram.DATA_POINTS,
        histogram.getPoints(),
        HistogramDataPointMarshaler::writeTo,
        context,
        DATA_POINT_WRITER_KEY);
    output.serializeEnum(
        Histogram.AGGREGATION_TEMPORALITY,
        MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  private static int calculateSize(
      HistogramDataPointMarshaler[] dataPoints, ProtoEnumInfo aggregationTemporality) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
    size += MarshalerUtil.sizeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }

  public static int calculateSize(HistogramData histogram, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Histogram.DATA_POINTS,
            histogram.getPoints(),
            HistogramDataPointMarshaler::calculateSize,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    size +=
        MarshalerUtil.sizeEnum(
            Histogram.AGGREGATION_TEMPORALITY,
            MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
    return size;
  }
}
