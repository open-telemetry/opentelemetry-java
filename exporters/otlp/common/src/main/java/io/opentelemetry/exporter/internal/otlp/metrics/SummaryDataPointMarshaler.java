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
import io.opentelemetry.proto.metrics.v1.internal.SummaryDataPoint;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import java.io.IOException;
import java.util.Collection;

final class SummaryDataPointMarshaler extends MarshalerWithSize {
  private final long startTimeUnixNano;
  private final long timeUnixNano;
  private final long count;
  private final double sum;
  private final ValueAtQuantileMarshaler[] quantileValues;
  private final MarshalerWithSize[] attributes;

  static SummaryDataPointMarshaler[] createRepeated(Collection<SummaryPointData> points) {
    SummaryDataPointMarshaler[] marshalers = new SummaryDataPointMarshaler[points.size()];
    int index = 0;
    for (SummaryPointData point : points) {
      marshalers[index++] = SummaryDataPointMarshaler.create(point);
    }
    return marshalers;
  }

  static SummaryDataPointMarshaler create(SummaryPointData point) {
    ValueAtQuantileMarshaler[] quantileMarshalers =
        ValueAtQuantileMarshaler.createRepeated(point.getValues());
    MarshalerWithSize[] attributeMarshalers =
        KeyValueMarshaler.createForAttributes(point.getAttributes());

    return new SummaryDataPointMarshaler(
        point.getStartEpochNanos(),
        point.getEpochNanos(),
        point.getCount(),
        point.getSum(),
        quantileMarshalers,
        attributeMarshalers);
  }

  private SummaryDataPointMarshaler(
      long startTimeUnixNano,
      long timeUnixNano,
      long count,
      double sum,
      ValueAtQuantileMarshaler[] quantileValues,
      MarshalerWithSize[] attributes) {
    super(calculateSize(startTimeUnixNano, timeUnixNano, count, sum, quantileValues, attributes));
    this.startTimeUnixNano = startTimeUnixNano;
    this.timeUnixNano = timeUnixNano;
    this.count = count;
    this.sum = sum;
    this.quantileValues = quantileValues;
    this.attributes = attributes;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    output.serializeFixed64(SummaryDataPoint.TIME_UNIX_NANO, timeUnixNano);
    output.serializeFixed64(SummaryDataPoint.COUNT, count);
    output.serializeDouble(SummaryDataPoint.SUM, sum);
    output.serializeRepeatedMessage(SummaryDataPoint.QUANTILE_VALUES, quantileValues);
    output.serializeRepeatedMessage(SummaryDataPoint.ATTRIBUTES, attributes);
  }

  public static void writeTo(Serializer output, SummaryPointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(SummaryDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    output.serializeFixed64(SummaryDataPoint.COUNT, point.getCount());
    output.serializeDoubleOptional(SummaryDataPoint.SUM, point.getSum());
    output.serializeRepeatedMessage(
        SummaryDataPoint.QUANTILE_VALUES,
        point.getValues(),
        ValueAtQuantileMarshaler::writeTo,
        context);
    KeyValueMarshaler.writeTo(output, context, SummaryDataPoint.ATTRIBUTES, point.getAttributes());
  }

  private static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      long count,
      double sum,
      ValueAtQuantileMarshaler[] quantileValues,
      MarshalerWithSize[] attributes) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.TIME_UNIX_NANO, timeUnixNano);
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.COUNT, count);
    size += MarshalerUtil.sizeDouble(SummaryDataPoint.SUM, sum);
    size += MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.QUANTILE_VALUES, quantileValues);
    size += MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.ATTRIBUTES, attributes);
    return size;
  }

  public static int calculateSize(SummaryPointData point, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            SummaryDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.COUNT, point.getCount());
    size += MarshalerUtil.sizeDoubleOptional(SummaryDataPoint.SUM, point.getSum());
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            SummaryDataPoint.QUANTILE_VALUES,
            point.getValues(),
            ValueAtQuantileMarshaler::calculateSize,
            context);
    size +=
        KeyValueMarshaler.calculateSize(
            SummaryDataPoint.ATTRIBUTES, point.getAttributes(), context);
    return size;
  }
}
