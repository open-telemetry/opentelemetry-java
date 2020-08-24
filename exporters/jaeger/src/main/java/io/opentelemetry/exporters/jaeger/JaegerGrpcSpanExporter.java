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

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporters.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporters.jaeger.proto.api_v2.Model;
import io.opentelemetry.sdk.common.export.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.ThreadSafe;

/** Exports spans to Jaeger via gRPC, using Jaeger's protobuf model. */
@ThreadSafe
public final class JaegerGrpcSpanExporter implements SpanExporter {
  public static final String DEFAULT_HOST_NAME = "unknown";
  public static final String DEFAULT_ENDPOINT = "localhost:14250";
  public static final String DEFAULT_SERVICE_NAME = DEFAULT_HOST_NAME;
  public static final long DEFAULT_DEADLINE_MS = TimeUnit.SECONDS.toMillis(1);

  private static final Logger logger = Logger.getLogger(JaegerGrpcSpanExporter.class.getName());
  private static final String CLIENT_VERSION_KEY = "jaeger.version";
  private static final String CLIENT_VERSION_VALUE = "opentelemetry-java";
  private static final String HOSTNAME_KEY = "hostname";
  private static final String IP_KEY = "ip";
  private static final String IP_DEFAULT = "0.0.0.0";
  private final CollectorServiceGrpc.CollectorServiceBlockingStub blockingStub;
  private final Model.Process process;
  private final ManagedChannel managedChannel;
  private final long deadlineMs;

  /**
   * Creates a new Jaeger gRPC Span Reporter with the given name, using the given channel.
   *
   * @param serviceName this service's name.
   * @param channel the channel to use when communicating with the Jaeger Collector.
   * @param deadlineMs max waiting time for the collector to process each span batch. When set to 0
   *     or to a negative value, the exporter will wait indefinitely.
   */
  private JaegerGrpcSpanExporter(String serviceName, ManagedChannel channel, long deadlineMs) {
    String hostname;
    String ipv4;

    if (serviceName == null || serviceName.trim().length() == 0) {
      throw new IllegalArgumentException("Service name must not be null or empty");
    }

    try {
      hostname = InetAddress.getLocalHost().getHostName();
      ipv4 = InetAddress.getLocalHost().getHostAddress();
    } catch (UnknownHostException e) {
      hostname = DEFAULT_HOST_NAME;
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
    this.deadlineMs = deadlineMs;
  }

  /**
   * Submits all the given spans in a single batch to the Jaeger collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    Collector.PostSpansRequest request =
        Collector.PostSpansRequest.newBuilder()
            .setBatch(
                Model.Batch.newBuilder()
                    .addAllSpans(Adapter.toJaeger(spans))
                    .setProcess(this.process)
                    .build())
            .build();

    try {
      CollectorServiceGrpc.CollectorServiceBlockingStub stub = this.blockingStub;
      if (deadlineMs > 0) {
        stub = stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
      }

      // for now, there's nothing to check in the response object
      //noinspection ResultOfMethodCallIgnored
      stub.postSpans(request);
      return CompletableResultCode.ofSuccess();
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Failed to export spans", e);
      return CompletableResultCode.ofFailure();
    }
  }

  /**
   * The Jaeger exporter does not batch spans, so this method will immediately return with success.
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
  public static class Builder extends ConfigBuilder<Builder> {
    private static final String KEY_SERVICE_NAME = "otel.jaeger.service.name";
    private static final String KEY_ENDPOINT = "otel.jaeger.endpoint";

    private String serviceName = DEFAULT_SERVICE_NAME;
    private String endpoint = DEFAULT_ENDPOINT;
    private ManagedChannel channel;
    private long deadlineMs = DEFAULT_DEADLINE_MS; // 1 second

    /**
     * Sets the service name to be used by this exporter. Required.
     *
     * @param serviceName the service name.
     * @return this.
     */
    public Builder setServiceName(String serviceName) {
      this.serviceName = serviceName;
      return this;
    }

    /**
     * Sets the managed chanel to use when communicating with the backend. Takes precedence over
     * {@link #setEndpoint(String)} if both are called.
     *
     * @param channel the channel to use.
     * @return this.
     */
    public Builder setChannel(ManagedChannel channel) {
      this.channel = channel;
      return this;
    }

    /**
     * Sets the Jaeger endpoint to connect to. Optional, defaults to "localhost:14250".
     *
     * @param endpoint The Jaeger endpoint URL, ex. "jaegerhost:14250".
     * @return this.
     * @since 0.7.0
     */
    public Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Sets the max waiting time for the collector to process each span batch. Optional.
     *
     * @param deadlineMs the max waiting time in millis.
     * @return this.
     */
    public Builder setDeadlineMs(long deadlineMs) {
      this.deadlineMs = deadlineMs;
      return this;
    }

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this.
     * @since 0.7.0
     */
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      String stringValue = getStringProperty(KEY_SERVICE_NAME, configMap);
      if (stringValue != null) {
        this.setServiceName(stringValue);
      }
      stringValue = getStringProperty(KEY_ENDPOINT, configMap);
      if (stringValue != null) {
        this.setEndpoint(stringValue);
      }
      return this;
    }

    /**
     * Constructs a new instance of the exporter based on the builder's values.
     *
     * @return a new exporter's instance.
     */
    public JaegerGrpcSpanExporter build() {
      if (channel == null) {
        channel = ManagedChannelBuilder.forTarget(endpoint).usePlaintext().build();
      }
      return new JaegerGrpcSpanExporter(serviceName, channel, deadlineMs);
    }

    private Builder() {}
  }
}
