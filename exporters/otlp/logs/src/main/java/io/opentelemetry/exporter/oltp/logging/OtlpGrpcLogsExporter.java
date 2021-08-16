/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.oltp.logging;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.opentelemetry.exporter.otlp.internal.LogAdapter;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceRequest;
import io.opentelemetry.proto.collector.logs.v1.ExportLogsServiceResponse;
import io.opentelemetry.proto.collector.logs.v1.LogsServiceGrpc;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.logs.data.LogRecord;
import io.opentelemetry.sdk.logs.export.LogExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public class OtlpGrpcLogsExporter implements LogExporter {
  private final ThrottlingLogger logger =
      new ThrottlingLogger(Logger.getLogger(OtlpGrpcLogsExporter.class.getName()));

  private final LogsServiceGrpc.LogsServiceFutureStub logsService;
  private final ManagedChannel managedChannel;
  private final long timeoutNanos;

  /**
   * Creates a new OTLP gRPC Logging Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the OpenTelemetry Collector.
   * @param timeoutNanos max waiting time for the collector to process each logging batch. When set
   *     to 0 or to a negative value, the exporter will wait indefinitely.
   */
  OtlpGrpcLogsExporter(ManagedChannel channel, long timeoutNanos) {
    this.managedChannel = channel;
    this.timeoutNanos = timeoutNanos;
    logsService = LogsServiceGrpc.newFutureStub(channel);
  }

  @Override
  public CompletableResultCode export(Collection<LogRecord> logs) {
    ExportLogsServiceRequest exportLogsServiceRequest =
        ExportLogsServiceRequest.newBuilder()
            .addAllResourceLogs(LogAdapter.toProtoResourceLogs(logs))
            .build();

    final CompletableResultCode result = new CompletableResultCode();
    LogsServiceGrpc.LogsServiceFutureStub exporter;
    if (timeoutNanos > 0) {
      exporter = logsService.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    } else {
      exporter = logsService;
    }

    Futures.addCallback(
        exporter.export(exportLogsServiceRequest),
        new FutureCallback<ExportLogsServiceResponse>() {
          @Override
          public void onSuccess(@Nullable ExportLogsServiceResponse response) {
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            Status status = Status.fromThrowable(t);
            switch (status.getCode()) {
              case UNIMPLEMENTED:
                logger.log(
                    Level.SEVERE,
                    "Failed to export logs. Server responded with UNIMPLEMENTED. "
                        + "This usually means that your collector is not configured with an otlp "
                        + "receiver in the \"pipelines\" section of the configuration. "
                        + "Full error message: "
                        + t.getMessage());
                break;
              case UNAVAILABLE:
                logger.log(
                    Level.SEVERE,
                    "Failed to export logs. Server is UNAVAILABLE. "
                        + "Make sure your collector is running and reachable from this network."
                        + t.getMessage());
                break;
              default:
                logger.log(
                    Level.WARNING, "Failed to export logs. Error message: " + t.getMessage());
                break;
            }
            logger.log(Level.FINEST, "Failed to export logs. Details follow: " + t);
            result.fail();
          }
        },
        MoreExecutors.directExecutor());
    return result;
  }

  /**
   * The OTLP exporter does not batch logging, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public CompletableResultCode flush() {
    return CompletableResultCode.ofSuccess();
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcLoggingExporterBuilder builder() {
    return new OtlpGrpcLoggingExporterBuilder();
  }

  /**
   * Returns a new {@link OtlpGrpcLogsExporter} reading the configuration values from the
   * environment and from system properties. System properties override values defined in the
   * environment. If a configuration value is missing, it uses the default value.
   *
   * @return a new {@link OtlpGrpcLogsExporter} instance.
   */
  public static OtlpGrpcLogsExporter getDefault() {
    return builder().build();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled. The channel is forcefully closed after a timeout.
   */
  @Override
  public CompletableResultCode shutdown() {
    try {
      managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.log(Level.WARNING, "Failed to shutdown the gRPC channel", e);
      return CompletableResultCode.ofFailure();
    }
    return CompletableResultCode.ofSuccess();
  }
}
