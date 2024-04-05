/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.ProtoEnumInfo;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Histogram;
import io.opentelemetry.sdk.metrics.data.HistogramData;
import java.io.IOException;

final class HistogramMarshaler extends MarshalerWithSize {
  private final HistogramDataPointMarshaler[] dataPoints;
  private final ProtoEnumInfo aggregationTemporality;

  static HistogramMarshaler create(HistogramData histogram) {
    HistogramDataPointMarshaler[] dataPointMarshalers =
        HistogramDataPointMarshaler.createRepeated(histogram.getPoints());
    return new HistogramMarshaler(
        dataPointMarshalers,
        MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  private HistogramMarshaler(
      HistogramDataPointMarshaler[] dataPoints, ProtoEnumInfo aggregationTemporality) {
    super(calculateSize(dataPoints, aggregationTemporality));
    this.dataPoints = dataPoints;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
    output.serializeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  private static int calculateSize(
      HistogramDataPointMarshaler[] dataPoints, ProtoEnumInfo aggregationTemporality) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
    size += MarshalerUtil.sizeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }
}
