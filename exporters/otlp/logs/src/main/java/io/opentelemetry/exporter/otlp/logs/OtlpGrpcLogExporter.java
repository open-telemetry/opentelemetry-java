/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import io.opentelemetry.exporter.otlp.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.otlp.internal.logs.LogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.logs.data.LogData;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.Collection;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcLogExporter implements LogExporter {

  private final GrpcExporter<LogsRequestMarshaler> delegate;

  /**
   * Returns a new {@link OtlpGrpcLogExporter} reading the configuration values from the environment
   * and from system properties. System properties override values defined in the environment. If a
   * configuration value is missing, it uses the default value.
   *
   * @return a new {@link OtlpGrpcLogExporter} instance.
   */
  public static OtlpGrpcLogExporter getDefault() {
    return builder().build();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcLogExporterBuilder builder() {
    return new OtlpGrpcLogExporterBuilder();
  }

  OtlpGrpcLogExporter(GrpcExporter<LogsRequestMarshaler> delegate) {
    this.delegate = delegate;
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogData> logs) {
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
