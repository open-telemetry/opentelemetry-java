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
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.logs.v1.internal.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceLogs. See {@link ResourceLogsMarshaler}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceLogsStatelessMarshaler
    implements StatelessMarshaler2<Resource, Map<InstrumentationScopeInfo, List<LogRecordData>>> {
  static final ResourceLogsStatelessMarshaler INSTANCE = new ResourceLogsStatelessMarshaler();
  private static final MarshalerContext.Key SCOPE_LOG_WRITER_KEY = MarshalerContext.key();
  private static final MarshalerContext.Key SCOPE_LOG_SIZE_CALCULATOR_KEY = MarshalerContext.key();

  @Override
  public void writeTo(
      Serializer output,
      Resource resource,
      Map<InstrumentationScopeInfo, List<LogRecordData>> scopeMap,
      MarshalerContext context)
      throws IOException {
    ResourceMarshaler resourceMarshaler = context.getData(ResourceMarshaler.class);
    output.serializeMessage(ResourceLogs.RESOURCE, resourceMarshaler);

    output.serializeRepeatedMessageWithContext(
        ResourceLogs.SCOPE_LOGS,
        scopeMap,
        InstrumentationScopeLogsStatelessMarshaler.INSTANCE,
        context,
        SCOPE_LOG_WRITER_KEY);

    output.serializeStringWithContext(ResourceLogs.SCHEMA_URL, resource.getSchemaUrl(), context);
  }

  @Override
  public int getBinarySerializedSize(
      Resource resource,
      Map<InstrumentationScopeInfo, List<LogRecordData>> scopeMap,
      MarshalerContext context) {

    int size = 0;

    ResourceMarshaler resourceMarshaler = ResourceMarshaler.create(resource);
    context.addData(resourceMarshaler);
    size += MarshalerUtil.sizeMessage(ResourceLogs.RESOURCE, resourceMarshaler);

    size +=
        StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
            ResourceLogs.SCOPE_LOGS,
            scopeMap,
            InstrumentationScopeLogsStatelessMarshaler.INSTANCE,
            context,
            SCOPE_LOG_SIZE_CALCULATOR_KEY);

    size +=
        StatelessMarshalerUtil.sizeStringWithContext(
            ResourceLogs.SCHEMA_URL, resource.getSchemaUrl(), context);

    return size;
  }
}
