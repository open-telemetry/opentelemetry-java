/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.logs;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.metrics.BoundLongCounter;
import io.opentelemetry.api.metrics.GlobalMeterProvider;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.api.metrics.Meter;
import io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.otlp.internal.logs.LogsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.logging.data.LogRecord;
import io.opentelemetry.sdk.logging.export.LogExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Exports logs using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcLogExporter implements LogExporter {
  private static final AttributeKey<String> EXPORTER_KEY = AttributeKey.stringKey("exporter");
  private static final AttributeKey<String> SUCCESS_KEY = AttributeKey.stringKey("success");
  private static final String EXPORTER_NAME = OtlpGrpcLogExporter.class.getSimpleName();
  private static final Attributes EXPORTER_NAME_Attributes =
      Attributes.of(EXPORTER_KEY, EXPORTER_NAME);
  private static final Attributes EXPORT_SUCCESS_ATTRIBUTES =
      Attributes.of(EXPORTER_KEY, EXPORTER_NAME, SUCCESS_KEY, "true");
  private static final Attributes EXPORT_FAILURE_ATTRIBUTES =
      Attributes.of(EXPORTER_KEY, EXPORTER_NAME, SUCCESS_KEY, "false");

  private static final Logger internalLogger =
      Logger.getLogger(OtlpGrpcLogExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final MarshalerLogsServiceGrpc.LogsServiceFutureStub logsService;

  private final ManagedChannel managedChannel;
  private final long timeoutNanos;
  private final BoundLongCounter logsSeen;
  private final BoundLongCounter logsExportedSuccess;
  private final BoundLongCounter logsExportedFailure;

  /**
   * Creates a new OTLP gRPC Logs Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the OpenTelemetry Collector.
   * @param timeoutNanos max waiting time for the collector to process each log batch. When set to 0
   *     or to a negative value, the exporter will wait indefinitely.
   * @param compressionEnabled whether or not to enable gzip compression.
   */
  OtlpGrpcLogExporter(ManagedChannel channel, long timeoutNanos, boolean compressionEnabled) {
    // TODO: telemetry schema version.
    Meter meter = GlobalMeterProvider.get().meterBuilder("io.opentelemetry.exporters.otlp").build();
    this.logsSeen =
        meter.counterBuilder("logsSeenByExporter").build().bind(EXPORTER_NAME_Attributes);
    LongCounter logsExportedCounter = meter.counterBuilder("logsExportedByExporter").build();
    this.logsExportedSuccess = logsExportedCounter.bind(EXPORT_SUCCESS_ATTRIBUTES);
    this.logsExportedFailure = logsExportedCounter.bind(EXPORT_FAILURE_ATTRIBUTES);
    this.managedChannel = channel;
    this.timeoutNanos = timeoutNanos;
    Codec codec = compressionEnabled ? new Codec.Gzip() : Codec.Identity.NONE;
    this.logsService =
        MarshalerLogsServiceGrpc.newFutureStub(channel).withCompression(codec.getMessageEncoding());
  }

  /**
   * Submits all the given logs in a single batch to the OpenTelemetry collector.
   *
   * @param logs the list of sampled logs to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<LogRecord> logs) {
    logsSeen.add(logs.size());
    LogsRequestMarshaler request = LogsRequestMarshaler.create(logs);

    final CompletableResultCode result = new CompletableResultCode();

    MarshalerLogsServiceGrpc.LogsServiceFutureStub exporter;
    if (timeoutNanos > 0) {
      exporter = logsService.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    } else {
      exporter = logsService;
    }

    Futures.addCallback(
        exporter.export(request),
        new FutureCallback<ExportLogsServiceResponse>() {
          @Override
          public void onSuccess(@Nullable ExportLogsServiceResponse response) {
            logsExportedSuccess.add(logs.size());
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            logsExportedFailure.add(logs.size());
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
                        + "Make sure your collector is running and reachable from this network. "
                        + "Full error message:"
                        + t.getMessage());
                break;
              default:
                logger.log(
                    Level.WARNING, "Failed to export logs. Error message: " + t.getMessage());
                break;
            }
            if (logger.isLoggable(Level.FINEST)) {
              logger.log(Level.FINEST, "Failed to export logs. Details follow: " + t);
            }
            result.fail();
          }
        },
        MoreExecutors.directExecutor());
    return result;
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static OtlpGrpcLogExporterBuilder builder() {
    return new OtlpGrpcLogExporterBuilder();
  }

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
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    if (managedChannel.isTerminated()) {
      return CompletableResultCode.ofSuccess();
    }
    this.logsSeen.unbind();
    this.logsExportedSuccess.unbind();
    this.logsExportedFailure.unbind();
    return ManagedChannelUtil.shutdownChannel(managedChannel);
  }

  // Visible for testing
  long getTimeoutNanos() {
    return timeoutNanos;
  }
}
