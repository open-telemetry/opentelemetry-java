/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshaler2;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaler;
import io.opentelemetry.proto.logs.v1.internal.ScopeLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.io.IOException;
import java.util.List;

/** See {@link InstrumentationScopeLogsMarshaler}. */
final class InstrumentationScopeLogsStatelessMarshaler
    implements StatelessMarshaler2<InstrumentationScopeInfo, List<LogRecordData>> {
  static final InstrumentationScopeLogsStatelessMarshaler INSTANCE =
      new InstrumentationScopeLogsStatelessMarshaler();

  @Override
  public void writeTo(
      Serializer output,
      InstrumentationScopeInfo instrumentationScope,
      List<LogRecordData> logs,
      MarshalerContext context)
      throws IOException {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        context.getData(InstrumentationScopeMarshaler.class);

    output.serializeMessage(ScopeLogs.SCOPE, instrumentationScopeMarshaler);
    output.serializeRepeatedMessageWithContext(
        ScopeLogs.LOG_RECORDS, logs, LogStatelessMarshaler.INSTANCE, context);
    output.serializeStringWithContext(
        ScopeLogs.SCHEMA_URL, instrumentationScope.getSchemaUrl(), context);
  }

  @Override
  public int getBinarySerializedSize(
      InstrumentationScopeInfo instrumentationScope,
      List<LogRecordData> logs,
      MarshalerContext context) {
    InstrumentationScopeMarshaler instrumentationScopeMarshaler =
        InstrumentationScopeMarshaler.create(instrumentationScope);
    context.addData(instrumentationScopeMarshaler);

    int size = 0;
    size += MarshalerUtil.sizeMessage(ScopeLogs.SCOPE, instrumentationScopeMarshaler);
    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            ScopeLogs.LOG_RECORDS, logs, LogStatelessMarshaler.INSTANCE, context);
    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            ScopeLogs.SCHEMA_URL, instrumentationScope.getSchemaUrl(), context);

    return size;
  }
}
