/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import io.opentelemetry.exporter.otlp.internal.logs.LogsRequestMarshaler;
import io.opentelemetry.exporter.otlp.internal.okhttp.OkHttpExporter;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via HTTP, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpHttpLogExporter implements LogExporter {

  private final OkHttpExporter<LogsRequestMarshaler> delegate;

  OtlpHttpLogExporter(OkHttpExporter<LogsRequestMarshaler> delegate) {
    this.delegate = delegate;
  }

  /**
   * Returns a new {@link OtlpHttpLogExporter} using the default values.
   *
   * @return a new {@link OtlpHttpLogExporter} instance.
   */
  public static OtlpHttpLogExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpHttpLogExporterBuilder builder() {
    return new OtlpHttpLogExporterBuilder();
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled Logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogData> logs) {
    LogsRequestMarshaler exportRequest = LogsRequestMarshaler.create(logs);
    return delegate.export(exportRequest, logs.size());
  }

  /** Shutdown the exporter. */
  @Override
  public CompletableResultCode shutdown() {
    return delegate.shutdown();
  }
}
