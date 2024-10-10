/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.traces;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public abstract class SpanReusableDataMarshaler {

  private final Deque<LowAllocationTraceRequestMarshaler> marshalerPool = new ArrayDeque<>();

  private final MemoryMode memoryMode;

  public SpanReusableDataMarshaler(MemoryMode memoryMode) {
    this.memoryMode = memoryMode;
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }

  public abstract CompletableResultCode doExport(Marshaler exportRequest, int numItems);

  public CompletableResultCode export(Collection<SpanData> spans) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationTraceRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationTraceRequestMarshaler();
      }
      LowAllocationTraceRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(spans);
      return doExport(exportMarshaler, spans.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
    TraceRequestMarshaler request = TraceRequestMarshaler.create(spans);
    return doExport(request, spans.size());
  }
}
