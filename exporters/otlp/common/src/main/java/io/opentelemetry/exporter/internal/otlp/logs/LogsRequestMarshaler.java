/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.marshal.MarshalerUtil;
import io.opentelemetry.exporter.internal.marshal.MarshalerWithSize;
import io.opentelemetry.exporter.internal.marshal.Serializer;
import io.opentelemetry.proto.collector.logs.v1.internal.ExportLogsServiceRequest;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.io.IOException;
import java.util.Collection;

/**
 * {@link Marshaler} to convert SDK {@link LogRecordData} to OTLP ExportLogsServiceRequest.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class LogsRequestMarshaler extends MarshalerWithSize {

  private final ResourceLogsMarshaler[] resourceLogsMarshalers;

  /**
   * Returns a {@link LogsRequestMarshaler} that can be used to convert the provided {@link
   * SpanData} into a serialized OTLP ExportLogsServiceRequest.
   */
  public static LogsRequestMarshaler create(Collection<LogRecordData> logs) {
    return new LogsRequestMarshaler(ResourceLogsMarshaler.create(logs));
  }

  private LogsRequestMarshaler(ResourceLogsMarshaler[] resourceLogsMarshalers) {
    super(
        MarshalerUtil.sizeRepeatedMessage(
            ExportLogsServiceRequest.RESOURCE_LOGS, resourceLogsMarshalers));
    this.resourceLogsMarshalers = resourceLogsMarshalers;
  }

  @Override
  public void writeTo(Serializer output) throws IOException {
    output.serializeRepeatedMessage(ExportLogsServiceRequest.RESOURCE_LOGS, resourceLogsMarshalers);
  }
}
