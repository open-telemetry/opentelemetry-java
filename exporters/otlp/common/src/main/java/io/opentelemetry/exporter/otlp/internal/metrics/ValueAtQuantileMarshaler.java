/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.SummaryDataPoint;
import io.opentelemetry.sdk.metrics.data.ValueAtPercentile;
import java.io.IOException;
import java.util.List;

final class ValueAtQuantileMarshaler extends MarshalerWithSize {
  private final double quantile;
  private final double value;

  static ValueAtQuantileMarshaler[] createRepeated(List<ValueAtPercentile> values) {
    int numValues = values.size();
    ValueAtQuantileMarshaler[] marshalers = new ValueAtQuantileMarshaler[numValues];
    for (int i = 0; i < numValues; i++) {
      marshalers[i] = ValueAtQuantileMarshaler.create(values.get(i));
    }
    return marshalers;
  }

  private static ValueAtQuantileMarshaler create(ValueAtPercentile value) {
    return new ValueAtQuantileMarshaler(value.getPercentile() / 100.0, value.getValue());
  }

  private ValueAtQuantileMarshaler(double quantile, double value) {
    super(calculateSize(quantile, value));
    this.quantile = quantile;
    this.value = value;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeDouble(SummaryDataPoint.ValueAtQuantile.QUANTILE, quantile);
    output.serializeDouble(SummaryDataPoint.ValueAtQuantile.VALUE, value);
  }

  private static int calculateSize(double quantile, double value) {
    int size = 0;
    size += MarshalerUtil.sizeDouble(SummaryDataPoint.ValueAtQuantile.QUANTILE, quantile);
    size += MarshalerUtil.sizeDouble(SummaryDataPoint.ValueAtQuantile.VALUE, value);
    return size;
  }
}
