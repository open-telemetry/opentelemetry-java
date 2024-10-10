/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public abstract class LogReusableDataMarshaler {

  private final Deque<LowAllocationLogsRequestMarshaler> marshalerPool = new ArrayDeque<>();

  private final MemoryMode memoryMode;

  public LogReusableDataMarshaler(MemoryMode memoryMode) {
    this.memoryMode = memoryMode;
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }

  public abstract CompletableResultCode doExport(Marshaler exportRequest, int numItems);

  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationLogsRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationLogsRequestMarshaler();
      }
      LowAllocationLogsRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(logs);
      return doExport(exportMarshaler, logs.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
    LogsRequestMarshaler request = LogsRequestMarshaler.create(logs);
    return doExport(request, logs.size());
  }
}
