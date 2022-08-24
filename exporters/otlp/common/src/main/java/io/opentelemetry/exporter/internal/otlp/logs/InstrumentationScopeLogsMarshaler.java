/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.logs.v1.internal.ScopeLogs;
import java.io.IOException;
import java.util.List;

final class InstrumentationScopeLogsMarshaler extends MarshalerWithSize {
  private final InstrumentationScopeMarshaler instrumentationScope;
  private final List<Marshaler> logMarshalers;
  private final byte[] schemaUrlUtf8;

  InstrumentationScopeLogsMarshaler(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<Marshaler> logMarshalers) {
    super(calculateSize(instrumentationScope, schemaUrlUtf8, logMarshalers));
    this.instrumentationScope = instrumentationScope;
    this.schemaUrlUtf8 = schemaUrlUtf8;
    this.logMarshalers = logMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ScopeLogs.SCOPE, instrumentationScope);
    output.serializeRepeatedMessage(ScopeLogs.LOG_RECORDS, logMarshalers);
    output.serializeString(ScopeLogs.SCHEMA_URL, schemaUrlUtf8);
  }

  private static int calculateSize(
      InstrumentationScopeMarshaler instrumentationScope,
      byte[] schemaUrlUtf8,
      List<Marshaler> logMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeLogs.SCOPE, instrumentationScope);
    size += MarshalerUtil.sizeBytes(ScopeLogs.SCHEMA_URL, schemaUrlUtf8);
    size += MarshalerUtil.sizeRepeatedMessage(ScopeLogs.LOG_RECORDS, logMarshalers);
    return size;
  }
}
