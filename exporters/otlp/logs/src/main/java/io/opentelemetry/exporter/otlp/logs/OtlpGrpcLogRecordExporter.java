/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcLogRecordExporter implements LogRecordExporter {

  private final GrpcExporter<LogsRequestMarshaler> delegate;

  /**
   * Returns a new {@link OtlpGrpcLogRecordExporter} using the default values.
   *
   * <p>To load configuration values from environment variables and system properties, use <a
   * href="https://github.com/open-telemetry/opentelemetry-java/tree/main/sdk-extensions/autoconfigure">opentelemetry-sdk-extension-autoconfigure</a>.
   *
   * @return a new {@link OtlpGrpcLogRecordExporter} instance.
   */
  public static OtlpGrpcLogRecordExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcLogRecordExporterBuilder builder() {
    return new OtlpGrpcLogRecordExporterBuilder();
  }

  OtlpGrpcLogRecordExporter(GrpcExporter<LogsRequestMarshaler> delegate) {
    this.delegate = delegate;
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    LogsRequestMarshaler request = LogsRequestMarshaler.create(logs);
    return delegate.export(request, logs.size());
  }

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
}
