/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.LowAllocationTraceRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exports spans using OTLP via HTTP, using OpenTelemetry's protobuf model.
 *
 * @since 1.5.0
 */
@ThreadSafe
public final class OtlpHttpSpanExporter implements SpanExporter {

  private static final boolean LOW_ALLOCATION_MODE = true;
  private static final Deque<LowAllocationTraceRequestMarshaler> requests = new ArrayDeque<>();

  private final HttpExporterBuilder<Marshaler> builder;
  private final HttpExporter<Marshaler> delegate;

  OtlpHttpSpanExporter(HttpExporterBuilder<Marshaler> builder, HttpExporter<Marshaler> delegate) {
    this.builder = builder;
    this.delegate = delegate;
  }

  /**
   * Returns a new {@link OtlpHttpSpanExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpHttpSpanExporter} instance.
   */
  public static OtlpHttpSpanExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpSpanExporterBuilder builder() {
    return new OtlpHttpSpanExporterBuilder();
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   *
   * @since 1.29.0
   */
  public OtlpHttpSpanExporterBuilder toBuilder() {
    return new OtlpHttpSpanExporterBuilder(builder.copy());
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    if (LOW_ALLOCATION_MODE) {
      LowAllocationTraceRequestMarshaler request = requests.poll();
      if (request == null) {
        request = new LowAllocationTraceRequestMarshaler();
      }
      LowAllocationTraceRequestMarshaler exportRequest = request;
      exportRequest.initialize(spans);
      return delegate
          .export(exportRequest, spans.size())
          .whenComplete(
              () -> {
                exportRequest.reset();
                requests.add(exportRequest);
              });
    } else {
      TraceRequestMarshaler exportRequest = TraceRequestMarshaler.create(spans);
      return delegate.export(exportRequest, spans.size());
    }
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

  /** Shutdown the exporter, releasing any resources and preventing subsequent exports. */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public String toString() {
    return "OtlpHttpSpanExporter{" + builder.toString(false) + "}";
  }
}
