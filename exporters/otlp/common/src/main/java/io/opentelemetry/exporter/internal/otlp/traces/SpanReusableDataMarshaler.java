/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiFunction;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class SpanReusableDataMarshaler {

  private final Deque<LowAllocationTraceRequestMarshaler> marshalerPool =
      new ConcurrentLinkedDeque<>();

  private final MemoryMode memoryMode;
  private final BiFunction<Marshaler, Integer, CompletableResultCode> doExport;

  public SpanReusableDataMarshaler(
      MemoryMode memoryMode, BiFunction<Marshaler, Integer, CompletableResultCode> doExport) {
    this.memoryMode = memoryMode;
    this.doExport = doExport;
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }

  public CompletableResultCode export(Collection<SpanData> spans) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationTraceRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationTraceRequestMarshaler();
      }
      LowAllocationTraceRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(spans);
      return doExport
          .apply(exportMarshaler, spans.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
    TraceRequestMarshaler request = TraceRequestMarshaler.create(spans);
    return doExport.apply(request, spans.size());
  }
}
