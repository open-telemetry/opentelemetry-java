/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.Summary;
import io.opentelemetry.sdk.metrics.data.SummaryData;
import java.io.IOException;

final class SummaryMarshaler extends MarshalerWithSize {
  private final SummaryDataPointMarshaler[] dataPoints;

  static SummaryMarshaler create(SummaryData summary) {
    SummaryDataPointMarshaler[] dataPointMarshalers =
        SummaryDataPointMarshaler.createRepeated(summary.getPoints());
    return new SummaryMarshaler(dataPointMarshalers);
  }

  private SummaryMarshaler(SummaryDataPointMarshaler[] dataPoints) {
    super(calculateSize(dataPoints));
    this.dataPoints = dataPoints;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(Summary.DATA_POINTS, dataPoints);
  }

  private static int calculateSize(SummaryDataPointMarshaler[] dataPoints) {
    int size = 0;
    size += MarshalerUtil.sizeRepeatedMessage(Summary.DATA_POINTS, dataPoints);
    return size;
  }
}
