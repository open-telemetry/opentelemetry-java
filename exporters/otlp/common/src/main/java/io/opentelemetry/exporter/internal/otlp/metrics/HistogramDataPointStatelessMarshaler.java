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
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.HistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.io.IOException;

/** See {@link HistogramDataPointMarshaler}. */
final class HistogramDataPointStatelessMarshaler implements StatelessMarshaler<HistogramPointData> {
  static final HistogramDataPointStatelessMarshaler INSTANCE =
      new HistogramDataPointStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, HistogramPointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(HistogramDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(HistogramDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    output.serializeFixed64(HistogramDataPoint.COUNT, point.getCount());
    output.serializeDoubleOptional(HistogramDataPoint.SUM, point.getSum());
    if (point.hasMin()) {
      output.serializeDoubleOptional(HistogramDataPoint.MIN, point.getMin());
    }
    if (point.hasMax()) {
      output.serializeDoubleOptional(HistogramDataPoint.MAX, point.getMax());
    }
    output.serializeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, point.getCounts());
    output.serializeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, point.getBoundaries());
    output.serializeRepeatedMessageWithContext(
        HistogramDataPoint.EXEMPLARS,
        point.getExemplars(),
        ExemplarStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessageWithContext(
        HistogramDataPoint.ATTRIBUTES,
        point.getAttributes(),
        KeyValueStatelessMarshaler.INSTANCE,
        context);
  }

  @Override
  public int getBinarySerializedSize(HistogramPointData point, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            HistogramDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.COUNT, point.getCount());
    size += MarshalerUtil.sizeDoubleOptional(HistogramDataPoint.SUM, point.getSum());
    if (point.hasMin()) {
      size += MarshalerUtil.sizeDoubleOptional(HistogramDataPoint.MIN, point.getMin());
    }
    if (point.hasMax()) {
      size += MarshalerUtil.sizeDoubleOptional(HistogramDataPoint.MAX, point.getMax());
    }
    size += MarshalerUtil.sizeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, point.getCounts());
    size +=
        MarshalerUtil.sizeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, point.getBoundaries());
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            HistogramDataPoint.EXEMPLARS,
            point.getExemplars(),
            ExemplarStatelessMarshaler.INSTANCE,
            context);
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            HistogramDataPoint.ATTRIBUTES,
            point.getAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);
    return size;
  }
}
