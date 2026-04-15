/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.AttributeKeyValueStatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.io.IOException;

/** See {@link ExponentialHistogramDataPointMarshaler}. */
final class ExponentialHistogramDataPointStatelessMarshaler
    implements StatelessMarshaler<ExponentialHistogramPointData> {
  static final ExponentialHistogramDataPointStatelessMarshaler INSTANCE =
      new ExponentialHistogramDataPointStatelessMarshaler();

  private ExponentialHistogramDataPointStatelessMarshaler() {}

  @Override
  public void writeTo(
      Serializer output, ExponentialHistogramPointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(
        ExponentialHistogramDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(ExponentialHistogramDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    output.serializeFixed64(ExponentialHistogramDataPoint.COUNT, point.getCount());
    output.serializeDoubleOptional(ExponentialHistogramDataPoint.SUM, point.getSum());
    if (point.hasMin()) {
      output.serializeDoubleOptional(ExponentialHistogramDataPoint.MIN, point.getMin());
    }
    if (point.hasMax()) {
      output.serializeDoubleOptional(ExponentialHistogramDataPoint.MAX, point.getMax());
    }
    output.serializeSInt32(ExponentialHistogramDataPoint.SCALE, point.getScale());
    output.serializeFixed64(ExponentialHistogramDataPoint.ZERO_COUNT, point.getZeroCount());
    output.serializeMessageWithContext(
        ExponentialHistogramDataPoint.POSITIVE,
        point.getPositiveBuckets(),
        ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
        context);
    output.serializeMessageWithContext(
        ExponentialHistogramDataPoint.NEGATIVE,
        point.getNegativeBuckets(),
        ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessageWithContext(
        ExponentialHistogramDataPoint.EXEMPLARS,
        point.getExemplars(),
        ExemplarStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessageWithContext(
        ExponentialHistogramDataPoint.ATTRIBUTES,
        point.getAttributes(),
        AttributeKeyValueStatelessMarshaler.INSTANCE,
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
    size += MarshalerUtil.sizeDoubleOptional(ExponentialHistogramDataPoint.SUM, point.getSum());
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
        StatelessMarshalerUtil.sizeMessageWithContext(
            ExponentialHistogramDataPoint.POSITIVE,
            point.getPositiveBuckets(),
            ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
            context);
    size +=
        StatelessMarshalerUtil.sizeMessageWithContext(
            ExponentialHistogramDataPoint.NEGATIVE,
            point.getNegativeBuckets(),
            ExponentialHistogramBucketsStatelessMarshaler.INSTANCE,
            context);
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            ExponentialHistogramDataPoint.EXEMPLARS,
            point.getExemplars(),
            ExemplarStatelessMarshaler.INSTANCE,
            context);
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            ExponentialHistogramDataPoint.ATTRIBUTES,
            point.getAttributes(),
            AttributeKeyValueStatelessMarshaler.INSTANCE,
            context);

    return size;
  }
}
