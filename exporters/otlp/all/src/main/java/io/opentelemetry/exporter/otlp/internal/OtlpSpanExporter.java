/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.LowAllocationTraceRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.exporter.otlp.stream.StreamExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class OtlpSpanExporter {
  protected final OtlpExporter<Marshaler> delegate;
  protected final MemoryMode memoryMode;
  private final Deque<LowAllocationTraceRequestMarshaler> marshalerPool = new ArrayDeque<>();

  public OtlpSpanExporter(StreamExporter<Marshaler> delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled spans to be exported.
   * @return the result of the operation
   */
  public CompletableResultCode export(Collection<SpanData> spans) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
        LowAllocationTraceRequestMarshaler marshaler = marshalerPool.poll();
        if (marshaler == null) {
          marshaler = new LowAllocationTraceRequestMarshaler();
        }
        LowAllocationTraceRequestMarshaler exportMarshaler = marshaler;
        exportMarshaler.initialize(spans);
        return delegate
            .export(exportMarshaler, spans.size())
            .whenComplete(
                () -> {
                  exportMarshaler.reset();
                  marshalerPool.add(exportMarshaler);
                });
      }
      // MemoryMode == MemoryMode.IMMUTABLE_DATA
      TraceRequestMarshaler request = TraceRequestMarshaler.create(spans);
      return delegate.export(request, spans.size());
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }
}
