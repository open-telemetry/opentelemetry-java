/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.logs;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.logs.v1.internal.InstrumentationLibraryLogs;
import java.io.IOException;
import java.util.List;

final class InstrumentationLibraryLogsMarshaler extends MarshalerWithSize {
  private final InstrumentationLibraryMarshaler instrumentationLibrary;
  private final List<Marshaler> logMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationLibraryLogsMarshaler(
      InstrumentationLibraryMarshaler instrumentationLibrary,
      byte[] schemaUrlUtf8,
      List<Marshaler> logMarshalers) {
    super(calculateSize(instrumentationLibrary, schemaUrlUtf8, logMarshalers));
    this.instrumentationLibrary = instrumentationLibrary;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.logMarshalers = logMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(
        InstrumentationLibraryLogs.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
    output.serializeRepeatedMessage(InstrumentationLibraryLogs.LOGS, logMarshalers);
    output.serializeString(InstrumentationLibraryLogs.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationLibraryMarshaler instrumentationLibrary,
      byte[] schemaUrlUtf8,
      List<Marshaler> logMarshalers) {
    int size = 0;
    size +=
        MarshalerUtil.sizeMessage(
            InstrumentationLibraryLogs.INSTRUMENTATION_LIBRARY, instrumentationLibrary);
    size += MarshalerUtil.sizeBytes(InstrumentationLibraryLogs.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(InstrumentationLibraryLogs.LOGS, logMarshalers);
    return size;
  }
}
