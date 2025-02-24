/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiFunction;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class LogReusableDataMarshaler {

  private final Deque<LowAllocationLogsRequestMarshaler> marshalerPool =
      new ConcurrentLinkedDeque<>();

  private final MemoryMode memoryMode;
  private final BiFunction<Marshaler, Integer, CompletableResultCode> doExport;

  public LogReusableDataMarshaler(
      MemoryMode memoryMode, BiFunction<Marshaler, Integer, CompletableResultCode> doExport) {
    this.memoryMode = memoryMode;
    this.doExport = doExport;
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }

  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationLogsRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationLogsRequestMarshaler();
      }
      LowAllocationLogsRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(logs);
      return doExport
          .apply(exportMarshaler, logs.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
    LogsRequestMarshaler request = LogsRequestMarshaler.create(logs);
    return doExport.apply(request, logs.size());
  }
}
