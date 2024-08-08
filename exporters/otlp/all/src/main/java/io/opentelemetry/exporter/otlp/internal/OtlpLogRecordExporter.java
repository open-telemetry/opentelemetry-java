/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.logs.LowAllocationLogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public abstract class OtlpLogRecordExporter implements LogRecordExporter {
  protected final OtlpExporter<Marshaler> delegate;
  protected final MemoryMode memoryMode;
  private final Deque<LowAllocationLogsRequestMarshaler> marshalerPool = new ArrayDeque<>();

  public OtlpLogRecordExporter(OtlpExporter<Marshaler> delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationLogsRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationLogsRequestMarshaler();
      }
      LowAllocationLogsRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(logs);
      return delegate
          .export(exportMarshaler, logs.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
    LogsRequestMarshaler request = LogsRequestMarshaler.create(logs);
    return delegate.export(request, logs.size());
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
