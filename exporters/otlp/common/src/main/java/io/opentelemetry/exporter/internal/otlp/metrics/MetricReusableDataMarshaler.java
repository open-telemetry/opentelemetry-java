/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.otlp.metrics;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.function.BiFunction;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class MetricReusableDataMarshaler {

  private final Deque<LowAllocationMetricsRequestMarshaler> marshalerPool =
      new ConcurrentLinkedDeque<>();

  private final MemoryMode memoryMode;
  private final BiFunction<Marshaler, Integer, CompletableResultCode> doExport;

  public MetricReusableDataMarshaler(
      MemoryMode memoryMode, BiFunction<Marshaler, Integer, CompletableResultCode> doExport) {
    this.memoryMode = memoryMode;
    this.doExport = doExport;
  }

  public MemoryMode getMemoryMode() {
    return memoryMode;
  }

  public CompletableResultCode export(Collection<MetricData> metrics) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationMetricsRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationMetricsRequestMarshaler();
      }
      LowAllocationMetricsRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(metrics);
      return doExport
          .apply(exportMarshaler, metrics.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
    MetricsRequestMarshaler request = MetricsRequestMarshaler.create(metrics);
    return doExport.apply(request, metrics.size());
  }
}
