/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.KeyValueStatelessMarshaler;
import io.opentelemetry.proto.metrics.v1.internal.SummaryDataPoint;
import io.opentelemetry.sdk.metrics.data.SummaryPointData;
import java.io.IOException;

/** See {@link SummaryDataPointMarshaler}. */
final class SummaryDataPointStatelessMarshaler implements StatelessMarshaler<SummaryPointData> {
  static final SummaryDataPointStatelessMarshaler INSTANCE =
      new SummaryDataPointStatelessMarshaler();

  private SummaryDataPointStatelessMarshaler() {}

  @Override
  public void writeTo(Serializer output, SummaryPointData point, MarshalerContext context)
      throws IOException {
    output.serializeFixed64(SummaryDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    output.serializeFixed64(SummaryDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    output.serializeFixed64(SummaryDataPoint.COUNT, point.getCount());
    output.serializeDouble(SummaryDataPoint.SUM, point.getSum());
    output.serializeRepeatedMessageWithContext(
        SummaryDataPoint.QUANTILE_VALUES,
        point.getValues(),
        ValueAtQuantileStatelessMarshaler.INSTANCE,
        context);
    output.serializeRepeatedMessageWithContext(
        SummaryDataPoint.ATTRIBUTES,
        point.getAttributes(),
        KeyValueStatelessMarshaler.INSTANCE,
        context);
  }

  @Override
  public int getBinarySerializedSize(SummaryPointData point, MarshalerContext context) {
    int size = 0;
    size +=
        MarshalerUtil.sizeFixed64(
            SummaryDataPoint.START_TIME_UNIX_NANO, point.getStartEpochNanos());
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.TIME_UNIX_NANO, point.getEpochNanos());
    size += MarshalerUtil.sizeFixed64(SummaryDataPoint.COUNT, point.getCount());
    size += MarshalerUtil.sizeDouble(SummaryDataPoint.SUM, point.getSum());
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            SummaryDataPoint.QUANTILE_VALUES,
            point.getValues(),
            ValueAtQuantileStatelessMarshaler.INSTANCE,
            context);
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            SummaryDataPoint.ATTRIBUTES,
            point.getAttributes(),
            KeyValueStatelessMarshaler.INSTANCE,
            context);
    return size;
  }
}
