/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Sum;
import io.opentelemetry.sdk.metrics.data.PointData;
import io.opentelemetry.sdk.metrics.data.SumData;
import java.io.IOException;
import java.util.List;

abstract class SumMarshaler extends Marshaler {

  abstract List<NumberDataPointMarshaler> getDataPoints();
  abstract ProtoEnumInfo getAggregationTemporality();
  abstract boolean getIsMonotonic();

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Sum.DATA_POINTS, dataPoints);
    output.serializeEnum(Sum.AGGREGATION_TEMPORALITY, aggregationTemporality);
    output.serializeBool(Sum.IS_MONOTONIC, isMonotonic);
  }

  protected static int calculateSize(
      List<NumberDataPointMarshaler> dataPoints,
      ProtoEnumInfo aggregationTemporality,
      boolean isMonotonic) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Sum.DATA_POINTS, dataPoints);
    size += MarshalerUtil.sizeEnum(Sum.AGGREGATION_TEMPORALITY, aggregationTemporality);
    size += MarshalerUtil.sizeBool(Sum.IS_MONOTONIC, isMonotonic);
    return size;
  }
}
