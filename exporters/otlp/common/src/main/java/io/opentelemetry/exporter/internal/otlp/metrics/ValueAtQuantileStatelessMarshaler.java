/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.SummaryDataPoint;
import io.opentelemetry.sdk.metrics.data.ValueAtQuantile;
import java.io.IOException;

/** See {@link ValueAtQuantileMarshaler}. */
final class ValueAtQuantileStatelessMarshaler implements StatelessMarshaler<ValueAtQuantile> {
  static final ValueAtQuantileStatelessMarshaler INSTANCE = new ValueAtQuantileStatelessMarshaler();

  @Override
  public void writeTo(Serializer output, ValueAtQuantile value, MarshalerContext context)
      throws IOException {
    output.serializeDouble(SummaryDataPoint.ValueAtQuantile.QUANTILE, value.getQuantile());
    output.serializeDouble(SummaryDataPoint.ValueAtQuantile.VALUE, value.getValue());
  }

  @Override
  public int getBinarySerializedSize(ValueAtQuantile value, MarshalerContext context) {
    return ValueAtQuantileMarshaler.calculateSize(value.getQuantile(), value.getValue());
  }
}
