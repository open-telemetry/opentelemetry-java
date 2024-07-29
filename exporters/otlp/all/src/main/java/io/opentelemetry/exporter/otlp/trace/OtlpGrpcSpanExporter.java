/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.LowAllocationTraceRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.StringJoiner;
import javax.annotation.concurrent.ThreadSafe;

/** Exports spans using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcSpanExporter implements SpanExporter {

  private final Deque<LowAllocationTraceRequestMarshaler> marshalerPool = new ArrayDeque<>();
  private final GrpcExporterBuilder<Marshaler> builder;
  private final GrpcExporter<Marshaler> delegate;
  private final MemoryMode memoryMode;

  /**
   * Returns a new {@link OtlpGrpcSpanExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpGrpcSpanExporter} instance.
   */
  public static OtlpGrpcSpanExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcSpanExporterBuilder builder() {
    return new OtlpGrpcSpanExporterBuilder();
  }

  OtlpGrpcSpanExporter(
      GrpcExporterBuilder<Marshaler> builder,
      GrpcExporter<Marshaler> delegate,
      MemoryMode memoryMode) {
    this.builder = builder;
    this.delegate = delegate;
    this.memoryMode = memoryMode;
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   *
   * @since 1.29.0
   */
  public OtlpGrpcSpanExporterBuilder toBuilder() {
    return new OtlpGrpcSpanExporterBuilder(builder.copy(), memoryMode);
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
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

  /**
   * The OTLP exporter does not batch spans, so this method will immediately return with success.
   *
   * @return always Success
   */
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

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcSpanExporter{", "}");
    joiner.add(builder.toString(false));
    joiner.add("memoryMode=" + memoryMode);
    return joiner.toString();
  }
}
