/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.HistogramDataPoint;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramPointData;
import java.io.IOException;
import java.util.Collection;
import java.util.List;

final class HistogramDataPointMarshaler extends MarshalerWithSize {
  private final long startTimeUnixNano;
  private final long timeUnixNano;
  private final long count;
  private final double sum;
  private final List<Long> bucketCounts;
  private final List<Double> explicitBounds;
  private final ExemplarMarshaler[] exemplars;
  private final KeyValueMarshaler[] attributes;

  static HistogramDataPointMarshaler[] createRepeated(Collection<DoubleHistogramPointData> points) {
    HistogramDataPointMarshaler[] marshalers = new HistogramDataPointMarshaler[points.size()];
    int index = 0;
    for (DoubleHistogramPointData point : points) {
      marshalers[index++] = HistogramDataPointMarshaler.create(point);
    }
    return marshalers;
  }

  static HistogramDataPointMarshaler create(DoubleHistogramPointData point) {
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(point.getAttributes());
    ExemplarMarshaler[] exemplarMarshalers = ExemplarMarshaler.createRepeated(point.getExemplars());

    return new HistogramDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getCount(),
        point.getSum(),
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
            bucketCounts,
            explicitBounds,
            exemplars,
            attributes));
    this.startTimeUnixNano = startTimeUnixNano;
    this.timeUnixNano = timeUnixNano;
    this.count = count;
    this.sum = sum;
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
    output.serializeDouble(HistogramDataPoint.SUM, sum);
    output.serializeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, bucketCounts);
    output.serializeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, explicitBounds);
    output.serializeRepeatedMessage(HistogramDataPoint.EXEMPLARS, exemplars);
    output.serializeRepeatedMessage(HistogramDataPoint.ATTRIBUTES, attributes);
  }

  private static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      long count,
      double sum,
      List<Long> bucketCounts,
      List<Double> explicitBounds,
      ExemplarMarshaler[] exemplars,
      KeyValueMarshaler[] attributes) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.TIME_UNIX_NANO, timeUnixNano);
    size += MarshalerUtil.sizeFixed64(HistogramDataPoint.COUNT, count);
    size += MarshalerUtil.sizeDouble(HistogramDataPoint.SUM, sum);
    size += MarshalerUtil.sizeRepeatedFixed64(HistogramDataPoint.BUCKET_COUNTS, bucketCounts);
    size += MarshalerUtil.sizeRepeatedDouble(HistogramDataPoint.EXPLICIT_BOUNDS, explicitBounds);
    size += MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.EXEMPLARS, exemplars);
    size += MarshalerUtil.sizeRepeatedMessage(HistogramDataPoint.ATTRIBUTES, attributes);
    return size;
  }
}
