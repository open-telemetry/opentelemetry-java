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
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogram;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import java.io.IOException;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ExponentialHistogramMarshaler extends MarshalerWithSize {
  private static final Object DATA_POINT_SIZE_CALCULATOR_KEY = new Object();
  private static final Object DATA_POINT_WRITER_KEY = new Object();

  private final ExponentialHistogramDataPointMarshaler[] dataPoints;
  private final ProtoEnumInfo aggregationTemporality;

  static ExponentialHistogramMarshaler create(ExponentialHistogramData histogram) {
    ExponentialHistogramDataPointMarshaler[] dataPoints =
        ExponentialHistogramDataPointMarshaler.createRepeated(histogram.getPoints());
    return new ExponentialHistogramMarshaler(
        dataPoints, MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  private ExponentialHistogramMarshaler(
      ExponentialHistogramDataPointMarshaler[] dataPointMarshalers,
      ProtoEnumInfo aggregationTemporality) {
    super(calculateSize(dataPointMarshalers, aggregationTemporality));
    this.dataPoints = dataPointMarshalers;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(ExponentialHistogram.DATA_POINTS, dataPoints);
    output.serializeEnum(ExponentialHistogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  public static void writeTo(
      Serializer output, ExponentialHistogramData histogram, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        ExponentialHistogram.DATA_POINTS,
        histogram.getPoints(),
        ExponentialHistogramDataPointMarshaler::writeTo,
        context,
        DATA_POINT_WRITER_KEY);
    output.serializeEnum(
        ExponentialHistogram.AGGREGATION_TEMPORALITY,
        MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  private static int calculateSize(
      ExponentialHistogramDataPointMarshaler[] dataPointMarshalers,
      ProtoEnumInfo aggregationTemporality) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(ExponentialHistogram.DATA_POINTS, dataPointMarshalers);
    size +=
        MarshalerUtil.sizeEnum(
            ExponentialHistogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }

  public static int calculateSize(ExponentialHistogramData histogram, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExponentialHistogram.DATA_POINTS,
            histogram.getPoints(),
            ExponentialHistogramDataPointMarshaler::calculateSize,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    size +=
        MarshalerUtil.sizeEnum(
            ExponentialHistogram.AGGREGATION_TEMPORALITY,
            MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
    return size;
  }
}
