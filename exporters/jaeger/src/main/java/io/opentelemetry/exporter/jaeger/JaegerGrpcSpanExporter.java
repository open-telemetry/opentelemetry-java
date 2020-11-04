/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.jaeger;

import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector.PostSpansRequest;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Collector.PostSpansResponse;
import io.opentelemetry.exporter.jaeger.proto.api_v2.CollectorServiceGrpc;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model;
import io.opentelemetry.exporter.jaeger.proto.api_v2.Model.Process;
import io.opentelemetry.sdk.common.CompletableResultCode;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
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
  private final CollectorServiceGrpc.CollectorServiceFutureStub stub;
  private final Model.Process.Builder processBuilder;
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

    this.processBuilder =
        Model.Process.newBuilder()
            .setServiceName(serviceName)
            .addTags(clientTag)
            .addTags(ipv4Tag)
            .addTags(hostnameTag);

    this.managedChannel = channel;
    this.stub = CollectorServiceGrpc.newFutureStub(channel);
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
    CollectorServiceGrpc.CollectorServiceFutureStub stub = this.stub;
    if (deadlineMs > 0) {
      stub = stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
    }

    List<Collector.PostSpansRequest> requests = new ArrayList<>();
    spans.stream()
        .collect(Collectors.groupingBy(SpanData::getResource))
        .forEach((resource, spanData) -> requests.add(buildRequest(resource, spanData)));

    List<ListenableFuture<PostSpansResponse>> listenableFutures = new ArrayList<>(requests.size());
    for (PostSpansRequest request : requests) {
      listenableFutures.add(stub.postSpans(request));
    }

    final CompletableResultCode result = new CompletableResultCode();
    Futures.addCallback(
        Futures.allAsList(listenableFutures),
        new FutureCallback<List<PostSpansResponse>>() {
          @Override
          public void onSuccess(@Nullable List<Collector.PostSpansResponse> response) {
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            logger.log(Level.WARNING, "Failed to export spans", t);
            result.fail();
          }
        },
        MoreExecutors.directExecutor());
    return result;
  }

  private Collector.PostSpansRequest buildRequest(Resource resource, List<SpanData> spans) {
    Process.Builder builder = this.processBuilder.clone();
    builder.addAllTags(Adapter.toKeyValues(resource.getAttributes()));

    return Collector.PostSpansRequest.newBuilder()
        .setBatch(
            Model.Batch.newBuilder()
                .addAllSpans(Adapter.toJaeger(spans))
                .setProcess(builder.build())
                .build())
        .build();
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
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Initiates an orderly shutdown in which preexisting calls continue but new calls are immediately
   * cancelled.
   */
  @Override
  public CompletableResultCode shutdown() {
    final CompletableResultCode result = new CompletableResultCode();
    managedChannel.notifyWhenStateChanged(
        ConnectivityState.SHUTDOWN,
        new Runnable() {
          @Override
          public void run() {
            result.succeed();
          }
        });
    managedChannel.shutdown();
    return result;
  }

  /** Builder utility for this exporter. */
  public static class Builder extends ConfigBuilder<Builder> {
    private static final String KEY_SERVICE_NAME = "otel.exporter.jaeger.service.name";
    private static final String KEY_ENDPOINT = "otel.exporter.jaeger.endpoint";

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
     * Sets the managed channel to use when communicating with the backend. Takes precedence over
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
