/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Sum;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.io.IOException;

final class SumMarshaler extends MarshalerWithSize {
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
}
