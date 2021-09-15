/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.KeyValueMarshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.SummaryDataPoint;
import io.opentelemetry.sdk.metrics.data.DoubleSummaryPointData;
import java.io.IOException;
import java.util.Collection;

final class SummaryDataPointMarshaler extends MarshalerWithSize {
  private final long startTimeUnixNano;
  private final long timeUnixNano;
  private final long count;
  private final double sum;
  private final ValueAtQuantileMarshaler[] quantileValues;
  private final KeyValueMarshaler[] attributes;

  static SummaryDataPointMarshaler[] createRepeated(Collection<DoubleSummaryPointData> points) {
    SummaryDataPointMarshaler[] marshalers = new SummaryDataPointMarshaler[points.size()];
    int index = 0;
    for (DoubleSummaryPointData point : points) {
      marshalers[index++] = SummaryDataPointMarshaler.create(point);
    }
    return marshalers;
  }

  static SummaryDataPointMarshaler create(DoubleSummaryPointData point) {
    ValueAtQuantileMarshaler[] quantileMarshalers =
        ValueAtQuantileMarshaler.createRepeated(point.getPercentileValues());
    KeyValueMarshaler[] attributeMarshalers =
        KeyValueMarshaler.createRepeated(point.getAttributes());

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
      KeyValueMarshaler[] attributes) {
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

  private static int calculateSize(
      long startTimeUnixNano,
      long timeUnixNano,
      long count,
      double sum,
      ValueAtQuantileMarshaler[] quantileValues,
      KeyValueMarshaler[] attributes) {
    int size = 0;
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, startTimeUnixNano);
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.TIME_UNIX_NANO, timeUnixNano);
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.COUNT, count);
    size += MarshalerUtil.sizeDouble(SummaryDataPoint.SUM, sum);
    size += MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.QUANTILE_VALUES, quantileValues);
    size += MarshalerUtil.sizeRepeatedMessage(SummaryDataPoint.ATTRIBUTES, attributes);
    return size;
  }
}
