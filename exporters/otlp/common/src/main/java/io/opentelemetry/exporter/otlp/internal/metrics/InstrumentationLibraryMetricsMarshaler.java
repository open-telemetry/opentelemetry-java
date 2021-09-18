/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.metrics;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.metrics.v1.internal.InstrumentationLibraryMetrics;
import java.io.IOException;
import java.util.List;

final class InstrumentationLibraryMetricsMarshaler extends MarshalerWithSize {
  private final InstrumentationLibraryMarshaler instrumentationLibrary;
  private final List<Marshaler> metricMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationLibraryMetricsMarshaler(
      InstrumentationLibraryMarshaler instrumentationLibrary,
      byte[] schemaUrlUtf8,
      List<Marshaler> metricMarshalers) {
    super(calculateSize(instrumentationLibrary, schemaUrlUtf8, metricMarshalers));
    this.instrumentationLibrary = instrumentationLibrary;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.metricMarshalers = metricMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(
        InstrumentationLibraryMetrics.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
    output.serializeRepeatedMessage(InstrumentationLibraryMetrics.METRICS, metricMarshalers);
    output.serializeString(InstrumentationLibraryMetrics.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationLibraryMarshaler instrumentationLibrary,
      byte[] schemaUrlUtf8,
      List<Marshaler> metricMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeMessage(
            InstrumentationLibraryMetrics.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
    size += MarshalerUtil.sizeBytes(InstrumentationLibraryMetrics.SCHEMA_URL, schemaUrlUtf8);
    size +=
        MarshalerUtil.sizeRepeatedMessage(InstrumentationLibraryMetrics.METRICS, metricMarshalers);
    return size;
  }
}
