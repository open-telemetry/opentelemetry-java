/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.KeyValueMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.HistogramDataPoint;
import io.opentelemetry.sdk.internal.PrimitiveLongList;
import io.opentelemetry.sdk.metrics.data.HistogramPointData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

final class HistogramDataPointMarshaler extends MarshalerWithSize {
  private final long startTimeUnixNano;
  private final long timeUnixNano;
  private final long count;
  private final double sum;
  private final boolean hasMin;
  private final double min;
  private final boolean hasMax;
  private final double max;
  private final List<Long> bucketCounts;
  private final List<Double> explicitBounds;
  private final ExemplarMarshaler[] exemplars;
  private final KeyValueMarshaler[] attributes;

  static HistogramDataPointMarshaler[] createRepeated(Collection<HistogramPointData> points) {
    HistogramDataPointMarshaler[] marshalers = new HistogramDataPointMarshaler[points.size()];
    int index = 0;
    for (HistogramPointData point : points) {
      marshalers[index++] = HistogramDataPointMarshaler.create(point);
    }
    return marshalers;
  }

  static HistogramDataPointMarshaler create(HistogramPointData point) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(point.getAttributes());
    ExemplarMarshaler[] exemplarMarshalers = ExemplarMarshaler.createRepeated(point.getExemplars());

    return new HistogramDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getCount(),
        point.getSum(),
        point.hasMin(),
        point.getMin(),
        point.hasMax(),
        point.getMax(),
        point.getCounts(),
        point.getBoundaries(),
        exemplarMarshalers,
        attributeMarshalers);
  }

  private HistogramDataPointMarshaler(
      long startTimeUnixNano,
      long timeUnixNano,
      long count,
      double sum,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      List<Long> bucketCounts,
      List<Double> explicitBounds,
      ExemplarMarshaler[] exemplars,
      KeyValueMarshaler[] attributes) {
    super(
        calculateSize(
            startTimeUnixNano,
            timeUnixNano,
            count,
            sum,
            hasMin,
            min,
            hasMax,
            max,
            bucketCounts,
            explicitBounds,
            exemplars,
            attributes));
    this.startTimeUnixNano = startTimeUnixNano;
    this.timeUnixNano = timeUnixNano;
    this.count = count;
    this.sum = sum;
    this.hasMin = hasMin;
    this.min = min;
    this.hasMax = hasMax;
    this.max = max;
    this.bucketCounts = bucketCounts;
    this.explicitBounds = explicitBounds;
    this.exemplars = exemplars;
    this.attributes = attributes;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(HistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    output.serializeFixed64(HistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
    output.serializeFixed64(HistogramDataPoint.COUNT, count);
    output.serializeDoubleOptional(HistogramDataPoint.SUM, sum);
    if (hasMin) {
      output.serializeDoubleOptional(HistogramDataPoint.MIN, min);
    }
    if (hasMax) {
      output.serializeDoubleOptional(HistogramDataPoint.MAX, max);
    }
    output.serializeRepeatedFixed64(
        HistogramDataPoint.BUCKET_COUNTS, PrimitiveLongList.toArray(bucketCounts));
    output.serializeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, explicitBounds);
    output.serializeRepeatedMessage(HistogramDataPoint.EXEMPLARS, exemplars);
    output.serializeRepeatedMessage(HistogramDataPoint.ATTRIBUTES, attributes);
  }

  public static void writeTo(Serializer output, HistogramPointData point, MarshalerContext context)
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
    output.serializeRepeatedMessage(
        HistogramDataPoint.EXEMPLARS, point.getExemplars(), ExemplarMarshaler::writeTo, context);
    KeyValueMarshaler.writeTo(
        output, context, HistogramDataPoint.ATTRIBUTES, point.getAttributes());
  }

  private static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      long count,
      double sum,
      boolean hasMin,
      double min,
      boolean hasMax,
      double max,
      List<Long> bucketCounts,
      List<Double> explicitBounds,
      ExemplarMarshaler[] exemplars,
      KeyValueMarshaler[] attributes) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.COUNT, count);
    size += MarshalerUtil.sizeDoubleOptional(HistogramDataPoint.SUM, sum);
    if (hasMin) {
      size += MarshalerUtil.sizeDoubleOptional(HistogramDataPoint.MIN, min);
    }
    if (hasMax) {
      size += MarshalerUtil.sizeDoubleOptional(HistogramDataPoint.MAX, max);
    }
    size += MarshalerUtil.sizeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, bucketCounts);
    size += MarshalerUtil.sizeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, explicitBounds);
    size += MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.EXEMPLARS, exemplars);
    size += MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.ATTRIBUTES, attributes);
    return size;
  }

  public static int calculateSize(HistogramPointData point, MarshalerContext context) {
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
        MarshalerUtil.sizeRepeatedMessage(
            HistogramDataPoint.EXEMPLARS,
            point.getExemplars(),
            ExemplarMarshaler::calculateSize,
            context);
    size +=
        KeyValueMarshaler.calculateSize(
            HistogramDataPoint.ATTRIBUTES, point.getAttributes(), context);
    return size;
  }
}
