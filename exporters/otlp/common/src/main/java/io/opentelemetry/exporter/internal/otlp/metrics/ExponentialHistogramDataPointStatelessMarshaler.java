/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.io.IOException;

final class ExponentialHistogramDataPointStatelessMarshaler
    implements StatelessMarshaler<ExponentialHistogramPointData> {
  static final ExponentialHistogramDataPointStatelessMarshaler INSTANCE =
      new ExponentialHistogramDataPointStatelessMarshaler();

  @Override
  public void writeTo(
      Serializer output, ExponentialHistogramPointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(
        ExponentialHistogramDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(ExponentialHistogramDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    output.serializeFixed64(ExponentialHistogramDataPoint.COUNT, point.getCount());
    output.serializeDouble(ExponentialHistogramDataPoint.SUM, point.getSum());
    if (point.hasMin()) {
      output.serializeDoubleOptional(ExponentialHistogramDataPoint.MIN, point.getMin());
    }
    if (point.hasMax()) {
      output.serializeDoubleOptional(ExponentialHistogramDataPoint.MAX, point.getMax());
    }
    output.serializeSInt32(ExponentialHistogramDataPoint.SCALE, point.getScale());
    output.serializeFixed64(ExponentialHistogramDataPoint.ZERO_COUNT, point.getZeroCount());
    output.serializeMessage(
        ExponentialHistogramDataPoint.POSITIVE,
        point.getPositiveBuckets(),
        ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
        context);
    output.serializeMessage(
        ExponentialHistogramDataPoint.NEGATIVE,
        point.getNegativeBuckets(),
        ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessage(
        ExponentialHistogramDataPoint.EXEMPLARS,
        point.getExemplars(),
        ExemplarStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessage(
        ExponentialHistogramDataPoint.ATTRIBUTES,
        point.getAttributes(),
        KeyValueStatelessMarshaler.INSTANCE,
        context);
  }

  @Override
  public int getBinarySerializedSize(
      ExponentialHistogramPointData point, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            ExponentialHistogramDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    size +=
        MarshalerUtil.sizeFixed64(
            ExponentialHistogramDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    size += MarshalerUtil.sizeFixed64(ExponentialHistogramDataPoint.COUNT, point.getCount());
    size += MarshalerUtil.sizeDouble(ExponentialHistogramDataPoint.SUM, point.getSum());
    if (point.hasMin()) {
      size += MarshalerUtil.sizeDoubleOptional(ExponentialHistogramDataPoint.MIN, point.getMin());
    }
    if (point.hasMax()) {
      size += MarshalerUtil.sizeDoubleOptional(ExponentialHistogramDataPoint.MAX, point.getMax());
    }
    size += MarshalerUtil.sizeSInt32(ExponentialHistogramDataPoint.SCALE, point.getScale());
    size +=
        MarshalerUtil.sizeFixed64(ExponentialHistogramDataPoint.ZERO_COUNT, point.getZeroCount());
    size +=
        MarshalerUtil.sizeMessage(
            ExponentialHistogramDataPoint.POSITIVE,
            point.getPositiveBuckets(),
            ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
            context);
    size +=
        MarshalerUtil.sizeMessage(
            ExponentialHistogramDataPoint.NEGATIVE,
            point.getNegativeBuckets(),
            ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
            context);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExponentialHistogramDataPoint.EXEMPLARS,
            point.getExemplars(),
            ExemplarStatelessMarshaler.INSTANCE,
            context);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExponentialHistogramDataPoint.ATTRIBUTES,
            point.getAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);

    return size;
  }
}
