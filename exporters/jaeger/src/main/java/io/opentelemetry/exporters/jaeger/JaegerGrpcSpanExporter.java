/*
 * Copyright 2019, OpenTelemetry Authors
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

package io.opentelemetry.exporters.jaeger;

import io.grpc.Deadline;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.trace.export.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/** Exports spans to Jaeger via gRPC, using Jaeger's protobuf model. */
@ThreadSafe
public final class JaegerGrpcSpanExporter implements SpanExporter {
  private static final Logger logger = Logger.getLogger(JaegerGrpcSpanExporter.class.getName());
  private static final String CLIENT_VERSION_KEY = "jaeger.version";
  private static final String CLIENT_VERSION_VALUE = "opentelemetry-java";
  private static final String HOSTNAME_KEY = "hostname";
  private static final String HOSTNAME_DEFAULT = "(unknown)";
  private static final String IP_KEY = "ip";
  private static final String IP_DEFAULT = "0.0.0.0";

  private final CollectorServiceGrpc.CollectorServiceBlockingStub blockingStub;
  private final Model.Process process;
  private final ManagedChannel managedChannel;
  private final long deadline;

  /**
   * Creates a new Jaeger gRPC Span Reporter with the given name, using the given channel.
   *
   * @param serviceName this service's name.
   * @param channel the channel to use when communicating with the Jaeger Collector.
   * @param deadline max waiting time for the collector to process each span batch. When set to 0 or
   *     to a negative value, the exporter will wait indefinitely.
   */
  private JaegerGrpcSpanExporter(String serviceName, ManagedChannel channel, long deadline) {
    String hostname;
    String ipv4;

    if (serviceName == null || serviceName.trim().length() == 0) {
      throw new IllegalArgumentException("Service name must not be null or empty");
    }

    try {
      hostname = InetAddress.getLocalHost().getHostName();
      ipv4 = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      hostname = HOSTNAME_DEFAULT;
      ipv4 = IP_DEFAULT;
    }

    Model.KeyValue clientTag =
        Model.KeyValue.newBuilder()
            .setKey(CLIENT_VERSION_KEY)
            .setVStr(CLIENT_VERSION_VALUE)
            .build();

    Model.KeyValue ipv4Tag = Model.KeyValue.newBuilder().setKey(IP_KEY).setVStr(ipv4).build();

    Model.KeyValue hostnameTag =
        Model.KeyValue.newBuilder().setKey(HOSTNAME_KEY).setVStr(hostname).build();

    this.process =
        Model.Process.newBuilder()
            .setServiceName(serviceName)
            .addTags(clientTag)
            .addTags(ipv4Tag)
            .addTags(hostnameTag)
            .build();

    this.managedChannel = channel;
    this.blockingStub = CollectorServiceGrpc.newBlockingStub(channel);
    this.deadline = deadline;
  }

  /**
   * Submits all the given spans in a single batch to the Jaeger collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public ResultCode export(List<SpanData> spans) {
    Model.Batch.Builder builder = Model.Batch.newBuilder();
    builder.addAllSpans(Adapter.toJaeger(spans));
    builder.setProcess(this.process);

    Collector.PostSpansRequest.Builder requestBuilder = Collector.PostSpansRequest.newBuilder();
    requestBuilder.setBatch(builder.build());
    Collector.PostSpansRequest request = requestBuilder.build();

    try {
      CollectorServiceGrpc.CollectorServiceBlockingStub stub = this.blockingStub;
      if (deadline > 0) {
        stub = stub.withDeadline(Deadline.after(deadline, TimeUnit.MILLISECONDS));
      }

      // for now, there's nothing to check in the response object
      //noinspection ResultOfMethodCallIgnored
      stub.postSpans(request);
      return ResultCode.SUCCESS;
    } catch (StatusRuntimeException e) {
      switch (e.getStatus().getCode()) {
        case DEADLINE_EXCEEDED:
        case UNAVAILABLE:
          return ResultCode.FAILED_RETRYABLE;
        default:
          return ResultCode.FAILED_NOT_RETRYABLE;
      }
    } catch (Throwable t) {
      return ResultCode.FAILED_NOT_RETRYABLE;
    }
  }

  /**
   * Creates a new builder instance.
   *
   * @return a new instance builder for this exporter
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
    private String serviceName;
    private ManagedChannel channel;
    private long deadline = 1_000; // ms

    /**
     * Sets the service name to be used by this exporter. Required.
     *
     * @param serviceName the service name
     * @return this builder's instance
     */
    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

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
     * @param deadline the max waiting time
     * @return this builder's instance
     */
    public Builder setDeadline(long deadline) {
      this.deadline = deadline;
      return this;
    }

    /**
     * Constructs a new instance of the exporter based on the builder's values.
     *
     * @return a new exporter's instance
     */
    public JaegerGrpcSpanExporter build() {
      return new JaegerGrpcSpanExporter(serviceName, channel, deadline);
    }
  }
}
