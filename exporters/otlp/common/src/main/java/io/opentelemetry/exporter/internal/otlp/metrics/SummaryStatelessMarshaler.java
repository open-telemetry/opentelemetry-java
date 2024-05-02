/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.metrics.v1.internal.Summary;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import java.io.IOException;

/** See {@link SummaryMarshaler}. */
final class SummaryStatelessMarshaler implements StatelessMarshaler<SummaryData> {
  static final SummaryStatelessMarshaler INSTANCE = new SummaryStatelessMarshaler();
  private static final MarshalerContext.Key DATA_POINT_SIZE_CALCULATOR_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key DATA_POINT_WRITER_KEY = MarshalerContext.key();

  @Override
  public void writeTo(Serializer output, SummaryData summary, MarshalerContext context)
      throws IOException {
    output.serializeRepeatedMessageWithContext(
        Summary.DATA_POINTS,
        summary.getPoints(),
        SummaryDataPointStatelessMarshaler.INSTANCE,
        context,
        DATA_POINT_WRITER_KEY);
  }

  @Override
  public int getBinarySerializedSize(SummaryData summary, MarshalerContext context) {
    int size = 0;
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            Summary.DATA_POINTS,
            summary.getPoints(),
            SummaryDataPointStatelessMarshaler.INSTANCE,
            context,
            DATA_POINT_SIZE_CALCULATOR_KEY);
    return size;
  }
}
