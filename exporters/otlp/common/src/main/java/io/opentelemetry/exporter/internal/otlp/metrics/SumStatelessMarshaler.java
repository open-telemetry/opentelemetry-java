/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.Sum;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.io.IOException;

final class SumStatelessMarshaler implements StatelessMarshaler<SumData<? extends PointData>> {
  static final SumStatelessMarshaler INSTANCE = new SumStatelessMarshaler();
  private static final MarshalerContext.Key DATA_POINT_SIZE_CALCULATOR_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key DATA_POINT_WRITER_KEY = MarshalerContext.key();

  @Override
  public void writeTo(Serializer output, SumData<? extends PointData> sum, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessage(
        Sum.DATA_POINTS,
        sum.getPoints(),
        NumberDataPointStatelessMarshaler.INSTANCE,
        context,
        DATA_POINT_WRITER_KEY);
    output.serializeEnum(
        Sum.AGGREGATION_TEMPORALITY,
        MetricsMarshalerUtil.mapToTemporality(sum.getAggregationTemporality()));
    output.serializeBool(Sum.IS_MONOTONIC, sum.isMonotonic());
  }

  @Override
  public int getBinarySerializedSize(SumData<? extends PointData> sum, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            Sum.DATA_POINTS,
            sum.getPoints(),
            NumberDataPointStatelessMarshaler.INSTANCE,
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
