/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerContext;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.exporter.internal.marshal.StatelessMarshalerUtil;
import io.opentelemetry.proto.collector.logs.v1.internal.ExportLogsServiceRequest;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.resources.Resource;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * {@link Marshaler} to convert SDK {@link LogRecordData} to OTLP ExportLogsServiceRequest. See
 * {@link LogsRequestMarshaler}.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * void marshal(LowAllocationLogRequestMarshaler requestMarshaler, OutputStream output,
 *     List<LogRecordData> logDataList) throws IOException {
 *   requestMarshaler.initialize(logDataList);
 *   try {
 *     requestMarshaler.writeBinaryTo(output);
 *   } finally {
 *     requestMarshaler.reset();
 *   }
 * }
 * }</pre>
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LowAllocationLogsRequestMarshaler extends Marshaler {
  private static final MarshalerContext.Key RESOURCE_LOG_SIZE_CALCULATOR_KEY =
      MarshalerContext.key();
  private static final MarshalerContext.Key RESOURCE_LOG_WRITER_KEY = MarshalerContext.key();

  private final MarshalerContext context = new MarshalerContext();

  @SuppressWarnings("NullAway")
  private Map<Resource, Map<InstrumentationScopeInfo, List<LogRecordData>>> resourceAndScopeMap;

  private int size;

  public void initialize(Collection<LogRecordData> logDataList) {
    resourceAndScopeMap = groupByResourceAndScope(context, logDataList);
    size = calculateSize(context, resourceAndScopeMap);
  }

  public void reset() {
    context.reset();
  }

  @Override
  public int getBinarySerializedSize() {
    return size;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    // serializing can be retried, reset the indexes, so we could call writeTo multiple times
    context.resetReadIndex();
    output.serializeRepeatedMessageWithContext(
        ExportLogsServiceRequest.RESOURCE_LOGS,
        resourceAndScopeMap,
        ResourceLogsStatelessMarshaler.INSTANCE,
        context,
        RESOURCE_LOG_WRITER_KEY);
  }

  private static int calculateSize(
      MarshalerContext context,
      Map<Resource, Map<InstrumentationScopeInfo, List<LogRecordData>>> resourceAndScopeMap) {
    return StatelessMarshalerUtil.sizeRepeatedMessageWithContext(
        ExportLogsServiceRequest.RESOURCE_LOGS,
        resourceAndScopeMap,
        ResourceLogsStatelessMarshaler.INSTANCE,
        context,
        RESOURCE_LOG_SIZE_CALCULATOR_KEY);
  }

  private static Map<Resource, Map<InstrumentationScopeInfo, List<LogRecordData>>>
      groupByResourceAndScope(MarshalerContext context, Collection<LogRecordData> logDataList) {

    if (logDataList.isEmpty()) {
      return Collections.emptyMap();
    }

    return StatelessMarshalerUtil.groupByResourceAndScope(
        logDataList,
        // TODO(anuraaga): Replace with an internal SdkData type of interface that exposes these
        // two.
        LogRecordData::getResource,
        LogRecordData::getInstrumentationScopeInfo,
        context);
  }
}
