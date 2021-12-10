/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ProtoEnumInfo;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.ExponentialHistogram;
import io.opentelemetry.sdk.metrics.data.ExponentialHistogramData;
import java.io.IOException;

public class ExponentialHistogramMarshaler extends MarshalerWithSize {
  private final ExponentialHistogramDataPointMarshaler[] dataPoints;
  private final ProtoEnumInfo aggregationTemporality;

  static ExponentialHistogramMarshaler create(ExponentialHistogramData histogramData) {
    ExponentialHistogramDataPointMarshaler[] dataPoints =
        ExponentialHistogramDataPointMarshaler.createRepeated(histogramData.getPoints());
    return new ExponentialHistogramMarshaler(
        dataPoints,
        MetricsMarshalerUtil.mapToTemporality(histogramData.getAggregationTemporality()));
  }

  private ExponentialHistogramMarshaler(
      ExponentialHistogramDataPointMarshaler[] dataPointMarshalers,
      ProtoEnumInfo aggregationTemporality) {
    super(calculateSize(dataPointMarshalers, aggregationTemporality));
    this.dataPoints = dataPointMarshalers;
    this.aggregationTemporality = aggregationTemporality;
  }

  @Override
  protected void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(ExponentialHistogram.DATA_POINTS, dataPoints);
    output.serializeEnum(ExponentialHistogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
  }

  private static int calculateSize(
      ExponentialHistogramDataPointMarshaler[] dataPointMarshalers,
      ProtoEnumInfo aggregationTemporality) {
    int size = 0;
    size +=
        MarshalerUtil.sizeRepeatedMessage(ExponentialHistogram.DATA_POINTS, dataPointMarshalers);
    size +=
        MarshalerUtil.sizeEnum(
            ExponentialHistogram.AGGREGATION_TEMPORALITY, aggregationTemporality);
    return size;
  }
}
