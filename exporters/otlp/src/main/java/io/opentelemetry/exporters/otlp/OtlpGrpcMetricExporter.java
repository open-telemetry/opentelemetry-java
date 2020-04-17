/*
 * Copyright 2020, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.exporters.otlp;

import io.grpc.ManagedChannel;
import io.opentelemetry.proto.collector.metrics.v1.ExportMetricsServiceRequest;
import io.opentelemetry.proto.collector.metrics.v1.MetricsServiceGrpc;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/** Exports spans using OTLP via gRPC, using OpenTelemetry's protobuf model. */
@ThreadSafe
public final class OtlpGrpcMetricExporter implements MetricExporter {
  private static final Logger logger = Logger.getLogger(OtlpGrpcMetricExporter.class.getName());

  private final MetricsServiceGrpc.MetricsServiceBlockingStub blockingStub;
  private final ManagedChannel managedChannel;
  private final long deadlineMs;

  /**
   * Creates a new Jaeger gRPC Metric Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the Jaeger Collector.
   * @param deadlineMs max waiting time for the collector to process each span batch. When set to 0
   *     or to a negative value, the exporter will wait indefinitely.
   */
  private OtlpGrpcMetricExporter(ManagedChannel channel, long deadlineMs) {
    this.managedChannel = channel;
    this.blockingStub = MetricsServiceGrpc.newBlockingStub(channel);
    this.deadlineMs = deadlineMs;
  }

  /**
   * Submits all the given spans in a single batch to the Jaeger collector.
   *
   * @param metrics the list of Metrics to be exported.
   * @return the result of the operation
   */
  @Override
  public ResultCode export(Collection<MetricData> metrics) {
    ExportMetricsServiceRequest exportMetricsServiceRequest =
        ExportMetricsServiceRequest.newBuilder()
            .addAllResourceMetrics(MetricAdapter.toProtoResourceMetrics(metrics))
            .build();

    try {
      MetricsServiceGrpc.MetricsServiceBlockingStub stub = this.blockingStub;
      if (deadlineMs > 0) {
        stub = stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
      }

      // for now, there's nothing to check in the response object
      // noinspection ResultOfMethodCallIgnored
      stub.export(exportMetricsServiceRequest);
      return ResultCode.SUCCESS;
    } catch (Throwable e) {
      return ResultCode.FAILURE;
    }
  }

  /**
   * The OTLP exporter does not batch metrics, so this method will immediately return with success.
   *
   * @return always Success
   */
  @Override
  public ResultCode flush() {
    return ResultCode.SUCCESS;
  }

  /**
   * Returns a new builder instance for this exporter.
   *
   * @return a new builder instance for this exporter.
   */
  public static Builder newBuilder() {
    return new Builder();
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

  /** Builder utility for this exporter. */
  public static class Builder {
    private ManagedChannel channel;
    private long deadlineMs = 1_000; // 1 second

    /**
     * Sets the managed chanel to use when communicating with the backend. Required.
     *
     * @param channel the channel to use
     * @return this builder's instance
     */
    public Builder setChannel(ManagedChannel channel) {
      this.channel = channel;
      return this;
    }

    /**
     * Sets the max waiting time for the collector to process each span batch. Optional.
     *
     * @param deadlineMs the max waiting time
     * @return this builder's instance
     */
    public Builder setDeadlineMs(long deadlineMs) {
      this.deadlineMs = deadlineMs;
      return this;
    }

    /**
     * Constructs a new instance of the exporter based on the builder's values.
     *
     * @return a new exporter's instance
     */
    public OtlpGrpcMetricExporter build() {
      return new OtlpGrpcMetricExporter(channel, deadlineMs);
    }

    private Builder() {}
  }
}
