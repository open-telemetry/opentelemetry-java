/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.stream.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpLogRecordExporter;
import io.opentelemetry.exporter.otlp.stream.StreamExporter;
import io.opentelemetry.exporter.otlp.stream.StreamExporterBuilder;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.io.OutputStream;
import java.util.Collection;
import java.util.StringJoiner;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpStdoutLogRecordExporter implements LogRecordExporter {

  private final StreamExporterBuilder<Marshaler> builder;

  private final OtlpLogRecordExporter otlpExporter;
  private final StreamExporter<Marshaler> streamExporter;

  /**
   * Returns a new {@link OtlpStdoutLogRecordExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpStdoutLogRecordExporter} instance.
   */
  public static OtlpStdoutLogRecordExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpStdoutLogRecordExporterBuilder builder() {
    return new OtlpStdoutLogRecordExporterBuilder();
  }

  OtlpStdoutLogRecordExporter(
      StreamExporterBuilder<Marshaler> builder,
      StreamExporter<Marshaler> streamExporter,
      MemoryMode memoryMode) {
    this.streamExporter = streamExporter;
    this.otlpExporter = new OtlpLogRecordExporter(streamExporter, memoryMode);
    this.builder = builder;
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   */
  public OtlpStdoutLogRecordExporterBuilder toBuilder() {
    return new OtlpStdoutLogRecordExporterBuilder(builder.copy(), otlpExporter.getMemoryMode());
  }

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcLogRecordExporter{", "}");
    joiner.add(builder.toString(false));
    joiner.add("memoryMode=" + otlpExporter.getMemoryMode());
    return joiner.toString();
  }

  public OutputStream getOutputStream() {
    return streamExporter.getOutputStream();
  }

  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    return otlpExporter.export(logs);
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
