/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Histogram;
import io.opentelemetry.sdk.metrics.data.DoubleHistogramData;
import java.io.IOException;

final class HistogramMarshaler extends MarshalerWithSize {
  private final HistogramDataPointMarshaler[] dataPoints;
  private final int aggregationTemporality;

  static HistogramMarshaler create(DoubleHistogramData histogram) {
    HistogramDataPointMarshaler[] dataPointMarshalers =
        HistogramDataPointMarshaler.createRepeated(histogram.getPoints());
    return new HistogramMarshaler(
        dataPointMarshalers,
        MetricsMarshalerUtil.mapToTemporality(histogram.getAggregationTemporality()));
  }

  private HistogramMarshaler(HistogramDataPointMarshaler[] dataPoints, int aggregationTemporality) {
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
      HistogramDataPointMarshaler[] dataPoints, int aggregationTemporality) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Histogram.DATA_POINTS, dataPoints);
    size += MarshalerUtil.sizeEnum(Histogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }
}
