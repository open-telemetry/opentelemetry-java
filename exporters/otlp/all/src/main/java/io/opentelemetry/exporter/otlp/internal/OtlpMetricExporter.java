/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.LowAllocationMetricsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.metrics.MetricsRequestMarshaler;
import io.opentelemetry.exporter.otlp.stream.StreamExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;

public class OtlpMetricExporter {
  protected final OtlpExporter<Marshaler> delegate;
  protected final MemoryMode memoryMode;
  private final Deque<LowAllocationMetricsRequestMarshaler> marshalerPool = new ArrayDeque<>();

  public OtlpMetricExporter(StreamExporter<Marshaler> delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of sampled spans to be exported.
   * @return the result of the operation
   */
  public CompletableResultCode export(Collection<MetricData> metrics) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
         LowAllocationMetricsRequestMarshaler marshaler = marshalerPool.poll();
         if (marshaler == null) {
           marshaler = new LowAllocationMetricsRequestMarshaler();
         }
         LowAllocationMetricsRequestMarshaler exportMarshaler = marshaler;
         exportMarshaler.initialize(metrics);
         return delegate
             .export(exportMarshaler, metrics.size())
             .whenComplete(
                 () -> {
                   exportMarshaler.reset();
                   marshalerPool.add(exportMarshaler);
                 });
       }
       // MemoryMode == MemoryMode.IMMUTABLE_DATA
       MetricsRequestMarshaler request = MetricsRequestMarshaler.create(metrics);
       return delegate.export(request, metrics.size());
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }
}
