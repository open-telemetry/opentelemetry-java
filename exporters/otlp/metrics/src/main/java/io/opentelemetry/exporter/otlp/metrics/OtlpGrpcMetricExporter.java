/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.Codec;
import io.grpc.ManagedChannel;
import io.grpc.Status;
import io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil;
import io.opentelemetry.exporter.otlp.internal.metrics.MetricsRequestMarshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.internal.ThrottlingLogger;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** Exports metrics using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcMetricExporter implements MetricExporter {

  private static final Logger internalLogger =
      Logger.getLogger(OtlpGrpcMetricExporter.class.getName());

  private final ThrottlingLogger logger = new ThrottlingLogger(internalLogger);

  private final MarshalerMetricsServiceGrpc.MetricsServiceFutureStub metricsService;
  private final ManagedChannel managedChannel;
  private final long timeoutNanos;

  /**
   * Creates a new OTLP gRPC Metric Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the OpenTelemetry Collector.
   * @param timeoutNanos max waiting time for the collector to process each metric batch. When set
   *     to 0 or to a negative value, the exporter will wait indefinitely.
   * @param compressionEnabled whether or not to enable gzip compression.
   */
  OtlpGrpcMetricExporter(ManagedChannel channel, long timeoutNanos, boolean compressionEnabled) {
    this.managedChannel = channel;
    this.timeoutNanos = timeoutNanos;
    Codec codec = compressionEnabled ? new Codec.Gzip() : Codec.Identity.NONE;
    this.metricsService =
        MarshalerMetricsServiceGrpc.newFutureStub(channel)
            .withCompression(codec.getMessageEncoding());
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    MetricsRequestMarshaler request = MetricsRequestMarshaler.create(metrics);

    final CompletableResultCode result = new CompletableResultCode();
    MarshalerMetricsServiceGrpc.MetricsServiceFutureStub exporter;
    if (timeoutNanos > 0) {
      exporter = metricsService.withDeadlineAfter(timeoutNanos, TimeUnit.NANOSECONDS);
    } else {
      exporter = metricsService;
    }

    Futures.addCallback(
        exporter.export(request),
        new FutureCallback<ExportMetricsServiceResponse>() {
          @Override
          public void onSuccess(@Nullable ExportMetricsServiceResponse response) {
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            Status status = Status.fromThrowable(t);
            switch (status.getCode()) {
              case UNIMPLEMENTED:
                logger.log(
                    Level.SEVERE,
                    "Failed to export metrics. Server responded with UNIMPLEMENTED. "
                        + "This usually means that your collector is not configured with an otlp "
                        + "receiver in the \"pipelines\" section of the configuration. "
                        + "Full error message: "
                        + t.getMessage());
                break;
              case UNAVAILABLE:
                logger.log(
                    Level.SEVERE,
                    "Failed to export metrics. Server is UNAVAILABLE. "
                        + "Make sure your collector is running and reachable from this network."
                        + t.getMessage());
                break;
              default:
                logger.log(
                    Level.WARNING, "Failed to export metrics. Error message: " + t.getMessage());
                break;
            }
            logger.log(Level.FINEST, "Failed to export metrics. Details follow: " + t);
            result.fail();
          }
        },
        MoreExecutors.directExecutor());
    return result;
  }

  /**
   * The OTLP exporter does not batch metrics, so this method will immediately return with success.
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
  public static OtlpGrpcMetricExporterBuilder builder() {
    return new OtlpGrpcMetricExporterBuilder();
  }

  /**
   * Returns a new {@link OtlpGrpcMetricExporter} reading the configuration values from the
   * environment and from system properties. System properties override values defined in the
   * environment. If a configuration value is missing, it uses the default value.
   *
   * @return a new {@link OtlpGrpcMetricExporter} instance.
   */
  public static OtlpGrpcMetricExporter getDefault() {
    return builder().build();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled. The channel is forcefully closed after a timeout.
   */
  @Override
  public CompletableResultCode shutdown() {
    if (managedChannel.isTerminated()) {
      return CompletableResultCode.ofSuccess();
    }
    return ManagedChannelUtil.shutdownChannel(managedChannel);
  }
}
