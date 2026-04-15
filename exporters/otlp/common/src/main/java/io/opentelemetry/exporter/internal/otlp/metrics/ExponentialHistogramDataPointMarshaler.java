/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramPointData;
import java.io.IOException;
import java.util.Collection;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class ExponentialHistogramDataPointMarshaler extends MarshalerWithSize {

  private final long startTimeUnixNano;
  private final long timeUnixNano;
  private final int scale;
  private final long count;
  private final long zeroCount;
  private final double sum;
  private final boolean hasMin;
  private final double min;
  private final boolean hasMax;
  private final double max;
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
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
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
            hasMin,
            min,
            hasMax,
            max,
            zeroCount,
            positiveBuckets,
            negativeBuckets,
            exemplarMarshalers,
            attributeMarshalers));
    this.startTimeUnixNano = startEpochNanos;
    this.timeUnixNano = epochNanos;
    this.scale = scale;
    this.sum = sum;
    this.hasMin = hasMin;
    this.min = min;
    this.hasMax = hasMax;
    this.max = max;
    this.count = count;
    this.zeroCount = zeroCount;
    this.positiveBuckets = positiveBuckets;
    this.negativeBuckets = negativeBuckets;
    this.attributes = attributeMarshalers;
    this.exemplars = exemplarMarshalers;
  }

  static ExponentialHistogramDataPointMarshaler create(ExponentialHistogramPointData point) {
    KeyValueMarshaler[] attributes = KeyValueMarshaler.createForAttributes(point.getAttributes());
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
        point.hasMin(),
        point.getMin(),
        point.hasMax(),
        point.getMax(),
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
    output.serializeDoubleOptional(ExponentialHistogramDataPoint.SUM, sum);
    if (hasMin) {
      output.serializeDoubleOptional(ExponentialHistogramDataPoint.MIN, min);
    }
    if (hasMax) {
      output.serializeDoubleOptional(ExponentialHistogramDataPoint.MAX, max);
    }
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
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
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
    size += MarshalerUtil.sizeDoubleOptional(ExponentialHistogramDataPoint.SUM, sum);
    if (hasMin) {
      size += MarshalerUtil.sizeDoubleOptional(ExponentialHistogramDataPoint.MIN, min);
    }
    if (hasMax) {
      size += MarshalerUtil.sizeDoubleOptional(ExponentialHistogramDataPoint.MAX, max);
    }
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
