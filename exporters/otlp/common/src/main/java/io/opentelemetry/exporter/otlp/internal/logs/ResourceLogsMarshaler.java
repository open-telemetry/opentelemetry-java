/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.logs;

import io.opentelemetry.exporter.otlp.internal.InstrumentationLibraryMarshaler;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.MarshalerUtil;
import io.opentelemetry.exporter.otlp.internal.MarshalerWithSize;
import io.opentelemetry.exporter.otlp.internal.ResourceMarshaler;
import io.opentelemetry.exporter.otlp.internal.Serializer;
import io.opentelemetry.proto.logs.v1.internal.ResourceLogs;
import io.opentelemetry.sdk.common.InstrumentationLibraryInfo;
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
  private final InstrumentationLibraryLogsMarshaler[] instrumentationLibraryLogsMarshalers;

  /** Returns Marshalers of ResourceLogs created by grouping the provided logRecords. */
  public static ResourceLogsMarshaler[] create(Collection<LogData> logs) {
    Map<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>> resourceAndLibraryMap =
        groupByResourceAndLibrary(logs);

    ResourceLogsMarshaler[] resourceLogsMarshalers =
        new ResourceLogsMarshaler[resourceAndLibraryMap.size()];
    int posResource = 0;
    for (Map.Entry<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>> entry :
        resourceAndLibraryMap.entrySet()) {
      final InstrumentationLibraryLogsMarshaler[] instrumentationLibrarySpansMarshalers =
          new InstrumentationLibraryLogsMarshaler[entry.getValue().size()];
      int posInstrumentation = 0;
      for (Map.Entry<InstrumentationLibraryInfo, List<Marshaler>> entryIs :
          entry.getValue().entrySet()) {
        instrumentationLibrarySpansMarshalers[posInstrumentation++] =
            new InstrumentationLibraryLogsMarshaler(
                InstrumentationLibraryMarshaler.create(entryIs.getKey()),
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
      InstrumentationLibraryLogsMarshaler[] instrumentationLibraryLogsMarshalers) {
    super(calculateSize(resourceMarshaler, schemaUrl, instrumentationLibraryLogsMarshalers));
    this.resourceMarshaler = resourceMarshaler;
    this.schemaUrl = schemaUrl;
    this.instrumentationLibraryLogsMarshalers = instrumentationLibraryLogsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeMessage(ResourceLogs.RESOURCE, resourceMarshaler);
    output.serializeRepeatedMessage(
        ResourceLogs.INSTRUMENTATION_LIBRARY_LOGS, instrumentationLibraryLogsMarshalers);
    output.serializeString(ResourceLogs.SCHEMA_URL, schemaUrl);
  }

  private static int calculateSize(
      ResourceMarshaler resourceMarshaler,
      byte[] schemaUrl,
      InstrumentationLibraryLogsMarshaler[] instrumentationLibraryLogsMarshalers) {
    int size = 0;
    size += MarshalerUtil.sizeMessage(ResourceLogs.RESOURCE, resourceMarshaler);
    size += MarshalerUtil.sizeBytes(ResourceLogs.SCHEMA_URL, schemaUrl);
    size +=
        MarshalerUtil.sizeRepeatedMessage(
            ResourceLogs.INSTRUMENTATION_LIBRARY_LOGS, instrumentationLibraryLogsMarshalers);
    return size;
  }

  private static Map<Resource, Map<InstrumentationLibraryInfo, List<Marshaler>>>
      groupByResourceAndLibrary(Collection<LogData> logs) {
    return MarshalerUtil.groupByResourceAndLibrary(
        logs,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        LogData::getResource,
        LogData::getInstrumentationLibraryInfo,
        LogMarshaler::create);
  }
}
