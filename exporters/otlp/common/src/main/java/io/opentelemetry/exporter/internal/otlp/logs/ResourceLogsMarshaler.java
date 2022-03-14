/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.otlp.InstrumentationScopeMarshaller;
import io.opentelemetry.exporter.internal.otlp.ResourceMarshaler;
import io.opentelemetry.proto.logs.v1.internal.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * A Marshaler of ResourceLogs.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ResourceLogsMarshaler extends MarshalerWithSize {
  private final ResourceMarshaler resourceMarshaler;
  private final byte[] schemaUrl;
  private final InstrumentationScopeLogsMarshaler[] instrumentationScopeLogsMarshalers;

  /** Returns Marshalers of ResourceLogs created by grouping the provided logRecords. */
  public static ResourceLogsMarshaler[] create(Collection<LogData> logs) {
    Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> resourceAndScopeMap =
        groupByResourceAndScope(logs);

    ResourceLogsMarshaler[] resourceLogsMarshalers =
        new ResourceLogsMarshaler[resourceAndScopeMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>> entry :
        resourceAndScopeMap.entrySet()) {
      InstrumentationScopeLogsMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationScopeLogsMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationScopeInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationScopeLogsMarshaler(
                InstrumentationScopeMarshaller.create(entryIs.getKey()),
                MarshalerUtil.toBytes(entryIs.getKey().getSchemaUrl()),
                entryIs.getValue());
      }
      resourceLogsMarshalers[posResource++] =
          new ResourceLogsMarshaler(
              ResourceMarshaler.create(entry.getKey()),
              MarshalerUtil.toBytes(entry.getKey().getSchemaUrl()),
              instrumentationLibrarySpansMarshalers);
    }

    return resourceLogsMarshalers;
  }

  ResourceLogsMarshaler(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeLogsMarshaler[] instrumentationScopeLogsMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationScopeLogsMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationScopeLogsMarshalers = instrumentationScopeLogsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceLogs.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceLogs.INSTRUMENTATION_LIBRARY_LOGS, instrumentationScopeLogsMarshalers);
    output.serializeString(ResourceLogs.SCHEMA_URL, schemaUrl);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationScopeLogsMarshaler[] instrumentationScopeLogsMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceLogs.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceLogs.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceLogs.INSTRUMENTATION_LIBRARY_LOGS, instrumentationScopeLogsMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<Marshaler>>>
      groupByResourceAndScope(Collection<LogData> logs) {
    return MarshalerUtil.groupByResourceAndScope(
        logs,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        LogData::getResource,
        LogData::getInstrumentationScopeInfo,
        LogMarshaler::create);
  }
}
