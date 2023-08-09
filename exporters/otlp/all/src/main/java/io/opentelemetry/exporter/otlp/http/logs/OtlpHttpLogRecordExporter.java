/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import io.opentelemetry.exporter.internal.http.HttpExporter;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exports logs using OTLP via HTTP, using OpenTelemetry's protobuf model.
 *
 * @since 1.27.0
 */
@ThreadSafe
public final class OtlpHttpLogRecordExporter implements LogRecordExporter {

  private final HttpExporterBuilder<LogsRequestMarshaler> builder;
  private final HttpExporter<LogsRequestMarshaler> delegate;

  OtlpHttpLogRecordExporter(
      HttpExporterBuilder<LogsRequestMarshaler> builder,
      HttpExporter<LogsRequestMarshaler> delegate) {
    this.builder = builder;
    this.delegate = delegate;
  }

  /**
   * Returns a new {@link OtlpHttpLogRecordExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpHttpLogRecordExporter} instance.
   */
  public static OtlpHttpLogRecordExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpLogRecordExporterBuilder builder() {
    return new OtlpHttpLogRecordExporterBuilder();
  }

  /**
   * Returns a builder with configuration values equal to those for this exporter.
   *
   * <p>IMPORTANT: Be sure to {@link #shutdown()} this instance if it will no longer be used.
   */
  public OtlpHttpLogRecordExporterBuilder toBuilder() {
    return new OtlpHttpLogRecordExporterBuilder(builder.copy());
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled Logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    LogsRequestMarshaler exportRequest = LogsRequestMarshaler.create(logs);
    return delegate.export(exportRequest, logs.size());
  }

  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }

  @Override
  public String toString() {
    return "OtlpHttpLogRecordExporter{" + builder.toString(false) + "}";
  }
}
