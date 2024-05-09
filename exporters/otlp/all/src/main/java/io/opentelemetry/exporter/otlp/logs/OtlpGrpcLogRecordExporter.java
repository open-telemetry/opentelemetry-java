/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import io.opentelemetry.exporter.internal.grpc.GrpcExporter;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import io.opentelemetry.exporter.internal.otlp.logs.LowAllocationLogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.logs.export.LogRecordExporter;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.StringJoiner;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model.
 *
 * @since 1.27.0
 */
@ThreadSafe
public final class OtlpGrpcLogRecordExporter implements LogRecordExporter {

  private final Deque<LowAllocationLogsRequestMarshaler> marshalerPool = new ArrayDeque<>();
  private final GrpcExporterBuilder<Marshaler> builder;
  private final GrpcExporter<Marshaler> delegate;
  private final MemoryMode memoryMode;

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

  OtlpGrpcLogRecordExporter(
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
  public OtlpGrpcLogRecordExporterBuilder toBuilder() {
    return new OtlpGrpcLogRecordExporterBuilder(builder.copy(), memoryMode);
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogRecordData> logs) {
    if (memoryMode == MemoryMode.REUSABLE_DATA) {
      LowAllocationLogsRequestMarshaler marshaler = marshalerPool.poll();
      if (marshaler == null) {
        marshaler = new LowAllocationLogsRequestMarshaler();
      }
      LowAllocationLogsRequestMarshaler exportMarshaler = marshaler;
      exportMarshaler.initialize(logs);
      return delegate
          .export(exportMarshaler, logs.size())
          .whenComplete(
              () -> {
                exportMarshaler.reset();
                marshalerPool.add(exportMarshaler);
              });
    }
    // MemoryMode == MemoryMode.IMMUTABLE_DATA
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

  @Override
  public String toString() {
    StringJoiner joiner = new StringJoiner(", ", "OtlpGrpcLogRecordExporter{", "}");
    joiner.add(builder.toString(false));
    joiner.add("memoryMode=" + memoryMode);
    return joiner.toString();
  }
}
