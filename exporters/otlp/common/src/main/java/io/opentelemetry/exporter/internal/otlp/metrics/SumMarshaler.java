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
import io.opentelemetry.proto.metrics.v1.internal.Sum;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.io.IOException;

final class SumMarshaler extends MarshalerWithSize {
  private static final Object DATA_POINT_SIZE_CALCULATOR_KEY = new Object();
  private static final Object DATA_POINT_WRITER_KEY = new Object();

  private final NumberDataPointMarshaler[] dataPoints;
  private final ProtoEnumInfo aggregationTemporality;
  private final boolean isMonotonic;

  static SumMarshaler create(SumData<? extends PointData> sum) {
    NumberDataPointMarshaler[] dataPointMarshalers =
        NumberDataPointMarshaler.createRepeated(sum.getPoints());

    return new SumMarshaler(
        dataPointMarshalers,
        MetricsMarshalerUtil.mapToTemporality(sum.getAggregationTemporality()),
        sum.isMonotonic());
  }

  private SumMarshaler(
      NumberDataPointMarshaler[] dataPoints,
      ProtoEnumInfo aggregationTemporality,
      boolean isMonotonic) {
    super(calculateSize(dataPoints, aggregationTemporality, isMonotonic));
    this.dataPoints = dataPoints;
    this.aggregationTemporality = aggregationTemporality;
    this.isMonotonic = isMonotonic;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Sum.DATA_POINTS, dataPoints);
    output.serializeEnum(Sum.AGGREGATION_TEMPORALITY, aggregationTemporality);
    output.serializeBool(Sum.IS_MONOTONIC, isMonotonic);
  }

  public static void writeTo(
      Serializer output, SumData<? extends PointData> sum, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        Sum.DATA_POINTS,
        sum.getPoints(),
        NumberDataPointMarshaler::writeTo,
        context,
        DATA_POINT_WRITER_KEY);
    output.serializeEnum(
        Sum.AGGREGATION_TEMPORALITY,
        MetricsMarshalerUtil.mapToTemporality(sum.getAggregationTemporality()));
    output.serializeBool(Sum.IS_MONOTONIC, sum.isMonotonic());
  }

  private static int calculateSize(
      NumberDataPointMarshaler[] dataPoints,
      ProtoEnumInfo aggregationTemporality,
      boolean isMonotonic) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Sum.DATA_POINTS, dataPoints);
    size += MarshalerUtil.sizeEnum(Sum.AGGREGATION_TEMPORALITY, aggregationTemporality);
    size += MarshalerUtil.sizeBool(Sum.IS_MONOTONIC, isMonotonic);
    return size;
  }

  public static int calculateSize(SumData<? extends PointData> sum, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Sum.DATA_POINTS,
            sum.getPoints(),
            NumberDataPointMarshaler::calculateSize,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    size +=
        MarshalerUtil.sizeEnum(
            Sum.AGGREGATION_TEMPORALITY,
            MetricsMarshalerUtil.mapToTemporality(sum.getAggregationTemporality()));
    size += MarshalerUtil.sizeBool(Sum.IS_MONOTONIC, sum.isMonotonic());
    return size;
  }
}
