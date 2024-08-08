/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream.trace;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpSpanExporter;
import io.opentelemetry.exporter.otlp.stream.StreamExporter;
import io.opentelemetry.exporter.otlp.stream.StreamExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;

import java.util.Collection;
import java.util.StringJoiner;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class StdoutSpanExporter implements SpanExporter {

  private final StreamExporterBuilder<Marshaler> builder;

  private final OtlpSpanExporter otlpExporter;
  private final StreamExporter<Marshaler> streamExporter;

  /**
   * Returns a new {@link StdoutSpanExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link StdoutSpanExporter} instance.
   */
  public static StdoutSpanExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static StdoutSpanExporterBuilder builder() {
    return new StdoutSpanExporterBuilder();
  }

  StdoutSpanExporter(
      StreamExporterBuilder<Marshaler> builder,
      StreamExporter<Marshaler> streamExporter,
      MemoryMode memoryMode) {
    this.streamExporter = streamExporter;
    this.otlpExporter = new OtlpSpanExporter(streamExporter, memoryMode);
    this.builder = builder;
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   */
  public StdoutSpanExporterBuilder toBuilder() {
    return new StdoutSpanExporterBuilder(builder.copy(), otlpExporter.getMemoryMode());
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcSpanExporter{", "}");
    joiner.add(builder.toString(false));
    joiner.add("memoryMode=" + otlpExporter.getMemoryMode());
    return joiner.toString();
  }

  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    return otlpExporter.export(spans);
  }

  @Override
  public CompletableResultCode flush() {
    return streamExporter.flush();
  }

  @Override
  public CompletableResultCode shutdown() {
    return streamExporter.shutdown();
  }
}
