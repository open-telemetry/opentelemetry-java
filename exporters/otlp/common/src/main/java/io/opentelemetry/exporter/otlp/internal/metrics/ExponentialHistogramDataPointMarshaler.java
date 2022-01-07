/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.io.IOException;
import java.util.Collection;

public class ExponentialHistogramDataPointMarshaler extends MarshalerWithSize {

  private final long startTimeUnixNano;
  private final long timeUnixNano;
  private final int scale;
  private final long count;
  private final long zeroCount;
  private final double sum;
  private final ExponentialHistogramBucketsMarshaler positiveBuckets;
  private final ExponentialHistogramBucketsMarshaler negativeBuckets;
  private final ExemplarMarshaler[] exemplars;
  private final KeyValueMarshaler[] attributes;

  private ExponentialHistogramDataPointMarshaler(
      long startEpochNanos,
      long epochNanos,
      int scale,
      long count,
      double sum,
      long zeroCount,
      ExponentialHistogramBucketsMarshaler positiveBuckets,
      ExponentialHistogramBucketsMarshaler negativeBuckets,
      KeyValueMarshaler[] attributeMarshalers,
      ExemplarMarshaler[] exemplarMarshalers) {
    super(
        calculateSize(
            startEpochNanos,
            epochNanos,
            scale,
            count,
            sum,
            zeroCount,
            positiveBuckets,
            negativeBuckets,
            exemplarMarshalers,
            attributeMarshalers));
    this.startTimeUnixNano = startEpochNanos;
    this.timeUnixNano = epochNanos;
    this.scale = scale;
    this.sum = sum;
    this.count = count;
    this.zeroCount = zeroCount;
    this.positiveBuckets = positiveBuckets;
    this.negativeBuckets = negativeBuckets;
    this.attributes = attributeMarshalers;
    this.exemplars = exemplarMarshalers;
  }

  static ExponentialHistogramDataPointMarshaler create(ExponentialHistogramPointData point) {
    KeyValueMarshaler[] attributes = KeyValueMarshaler.createRepeated(point.getAttributes());
    ExemplarMarshaler[] exemplars = ExemplarMarshaler.createRepeated(point.getExemplars());

    ExponentialHistogramBucketsMarshaler positiveBuckets =
        ExponentialHistogramBucketsMarshaler.create(point.getPositiveBuckets());
    ExponentialHistogramBucketsMarshaler negativeBuckets =
        ExponentialHistogramBucketsMarshaler.create(point.getNegativeBuckets());

    return new ExponentialHistogramDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getScale(),
        point.getCount(),
        point.getSum(),
        point.getZeroCount(),
        positiveBuckets,
        negativeBuckets,
        attributes,
        exemplars);
  }

  static ExponentialHistogramDataPointMarshaler[] createRepeated(
      Collection<ExponentialHistogramPointData> points) {
    ExponentialHistogramDataPointMarshaler[] marshalers =
        new ExponentialHistogramDataPointMarshaler[points.size()];
    int index = 0;
    for (ExponentialHistogramPointData point : points) {
      marshalers[index++] = ExponentialHistogramDataPointMarshaler.create(point);
    }
    return marshalers;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(ExponentialHistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    output.serializeFixed64(ExponentialHistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
    output.serializeFixed64(ExponentialHistogramDataPoint.COUNT, count);
    output.serializeDouble(ExponentialHistogramDataPoint.SUM, sum);
    output.serializeSInt32(ExponentialHistogramDataPoint.SCALE, scale);
    output.serializeFixed64(ExponentialHistogramDataPoint.ZERO_COUNT, zeroCount);
    output.serializeMessage(ExponentialHistogramDataPoint.POSITIVE, positiveBuckets);
    output.serializeMessage(ExponentialHistogramDataPoint.NEGATIVE, negativeBuckets);
    output.serializeRepeatedMessage(ExponentialHistogramDataPoint.EXEMPLARS, exemplars);
    output.serializeRepeatedMessage(ExponentialHistogramDataPoint.ATTRIBUTES, attributes);
  }

  private static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      int scale,
      long count,
      double sum,
      long zeroCount,
      ExponentialHistogramBucketsMarshaler positiveBucketMarshaler,
      ExponentialHistogramBucketsMarshaler negativeBucketMarshaler,
      ExemplarMarshaler[] exemplarMarshalers,
      KeyValueMarshaler[] attributesMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            ExponentialHistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(ExponentialHistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
    size += MarshalerUtil.sizeSInt32(ExponentialHistogramDataPoint.SCALE, scale);
    size += MarshalerUtil.sizeFixed64(ExponentialHistogramDataPoint.COUNT, count);
    size += MarshalerUtil.sizeDouble(ExponentialHistogramDataPoint.SUM, sum);
    size += MarshalerUtil.sizeFixed64(ExponentialHistogramDataPoint.ZERO_COUNT, zeroCount);
    size +=
        MarshalerUtil.sizeMessage(ExponentialHistogramDataPoint.POSITIVE, positiveBucketMarshaler);
    size +=
        MarshalerUtil.sizeMessage(ExponentialHistogramDataPoint.NEGATIVE, negativeBucketMarshaler);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExponentialHistogramDataPoint.EXEMPLARS, exemplarMarshalers);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ExponentialHistogramDataPoint.ATTRIBUTES, attributesMarshalers);
    return size;
  }
}
