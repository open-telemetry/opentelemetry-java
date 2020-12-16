/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ManagedChannel;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceResponse;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc.MetricsServiceFutureStub;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.extension.otproto.MetricAdapter;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exports metrics using OTLP via gRPC, using OpenTelemetry's protobuf model.
 *
 * <p>Configuration options for {@link OtlpGrpcMetricExporter} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link OtlpGrpcMetricExporter}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.exporter.otlp.metric.timeout}: to set the max waiting time allowed to send each
 *       span batch.
 *   <li>{@code otel.exporter.otlp.metric.endpoint}: to set the endpoint to connect to.
 *   <li>{@code otel.exporter.otlp.metric.insecure}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code otel.exporter.otlp.metric.headers}: the headers associated with the requests.
 * </ul>
 *
 * <p>For environment variables, {@link OtlpGrpcMetricExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_TIMEOUT}: to set the max waiting time allowed to send each
 *       span batch.
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_ENDPOINT}: to set the endpoint to connect to.
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_INSECURE}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code OTEL_EXPORTER_OTLP_METRIC_HEADERS}: the headers associated with the requests.
 * </ul>
 *
 * <p>In both cases, if a property is missing, the name without "span" is used to resolve the value.
 */
@ThreadSafe
public final class OtlpGrpcMetricExporter implements MetricExporter {
  public static final String DEFAULT_ENDPOINT = "localhost:4317";
  public static final long DEFAULT_DEADLINE_MS = TimeUnit.SECONDS.toMillis(10);

  private static final Logger logger = Logger.getLogger(OtlpGrpcMetricExporter.class.getName());

  private final MetricsServiceFutureStub metricsService;
  private final ManagedChannel managedChannel;
  private final long deadlineMs;

  /**
   * Creates a new OTLP gRPC Metric Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the OpenTelemetry Collector.
   * @param deadlineMs max waiting time for the collector to process each metric batch. When set to
   *     0 or to a negative value, the exporter will wait indefinitely.
   */
  OtlpGrpcMetricExporter(ManagedChannel channel, long deadlineMs) {
    this.managedChannel = channel;
    this.deadlineMs = deadlineMs;
    metricsService = MetricsServiceGrpc.newFutureStub(channel);
  }

  /**
   * Submits all the given metrics in a single batch to the OpenTelemetry collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<MetricData> metrics) {
    ExportMetricsServiceRequest exportMetricsServiceRequest =
        ExportMetricsServiceRequest.newBuilder()
            .addAllResourceMetrics(MetricAdapter.toProtoResourceMetrics(metrics))
            .build();

    final CompletableResultCode result = new CompletableResultCode();
    MetricsServiceFutureStub exporter;
    if (deadlineMs > 0) {
      exporter = metricsService.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
    } else {
      exporter = metricsService;
    }

    Futures.addCallback(
        exporter.export(exportMetricsServiceRequest),
        new FutureCallback<ExportMetricsServiceResponse>() {
          @Override
          public void onSuccess(@Nullable ExportMetricsServiceResponse response) {
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            logger.log(Level.WARNING, "Failed to export metrics", t);
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
    return builder().readEnvironmentVariables().readSystemProperties().build();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled. The channel is forcefully closed after a timeout.
   */
  @Override
  public void shutdown() {
    try {
      managedChannel.shutdown().awaitTermination(5, TimeUnit.SECONDS);
    } catch (InterruptedException e) {
      logger.log(Level.WARNING, "Failed to shutdown the gRPC channel", e);
    }
  }
}
