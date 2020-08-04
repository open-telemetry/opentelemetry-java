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

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import com.google.common.base.Splitter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.data.SpanData;
import io.opentelemetry.sdk.trace.export.SpanExporter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * Exports spans using OTLP via gRPC, using OpenTelemetry's protobuf model.
 *
 * <p>Configuration options for {@link OtlpGrpcSpanExporter} can be read from system properties,
 * environment variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link OtlpGrpcSpanExporter}
 * will look for the following names:
 *
 * <ul>
 *   <li>{@code otel.otlp.span.timeout}: to set the max waiting time allowed to send each span
 *       batch.
 *   <li>{@code otel.otlp.endpoint}: to set the endpoint to connect to.
 *   <li>{@code otel.otlp.use.tls}: to set use or not TLS.
 *   <li>{@code otel.otlp.metadata} to set key-value pairs separated by semicolon to pass as request
 *       metadata.
 * </ul>
 *
 * <p>For environment variables, {@link OtlpGrpcSpanExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_OTLP_SPAN_TIMEOUT}: to set the max waiting time allowed to send each span
 *       batch.
 *   <li>{@code OTEL_OTLP_ENDPOINT}: to set the endpoint to connect to.
 *   <li>{@code OTEL_OTLP_USE_TLS}: to set use or not TLS.
 *   <li>{@code OTEL_OTLP_METADATA}: to set key-value pairs separated by semicolon to pass as
 *       request metadata.
 * </ul>
 */
@ThreadSafe
public final class OtlpGrpcSpanExporter implements SpanExporter {
  public static final String DEFAULT_ENDPOINT = "localhost:55680";
  public static final long DEFAULT_DEADLINE_MS = TimeUnit.SECONDS.toMillis(1);

  private static final Logger logger = Logger.getLogger(OtlpGrpcSpanExporter.class.getName());

  private final TraceServiceGrpc.TraceServiceBlockingStub blockingStub;
  private final ManagedChannel managedChannel;
  private final long deadlineMs;

  /**
   * Creates a new OTLP gRPC Span Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the OpenTelemetry Collector.
   * @param deadlineMs max waiting time for the collector to process each span batch. When set to 0
   *     or to a negative value, the exporter will wait indefinitely.
   */
  private OtlpGrpcSpanExporter(ManagedChannel channel, long deadlineMs) {
    this.managedChannel = channel;
    this.blockingStub = TraceServiceGrpc.newBlockingStub(channel);
    this.deadlineMs = deadlineMs;
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public ResultCode export(Collection<SpanData> spans) {
    ExportTraceServiceRequest exportTraceServiceRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(spans))
            .build();

    try {
      TraceServiceGrpc.TraceServiceBlockingStub stub = this.blockingStub;
      if (deadlineMs > 0) {
        stub = stub.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
      }

      // for now, there's nothing to check in the response object
      // noinspection ResultOfMethodCallIgnored
      stub.export(exportTraceServiceRequest);
      return ResultCode.SUCCESS;
    } catch (Throwable e) {
      logger.log(Level.WARNING, "Failed to export spans", e);
      return ResultCode.FAILURE;
    }
  }

  /**
   * The OTLP exporter does not batch spans, so this method will immediately return with success.
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
   * Returns a new {@link OtlpGrpcSpanExporter} reading the configuration values from the
   * environment and from system properties. System properties override values defined in the
   * environment. If a configuration value is missing, it uses the default value.
   *
   * @return a new {@link OtlpGrpcSpanExporter} instance.
   * @since 0.5.0
   */
  public static OtlpGrpcSpanExporter getDefault() {
    return newBuilder().readEnvironmentVariables().readSystemProperties().build();
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
    private static final String KEY_SPAN_TIMEOUT = "otel.otlp.span.timeout";
    private static final String KEY_ENDPOINT = "otel.otlp.endpoint";
    private static final String KEY_USE_TLS = "otel.otlp.use.tls";
    private static final String KEY_METADATA = "otel.otlp.metadata";
    private ManagedChannel channel;
    private long deadlineMs = DEFAULT_DEADLINE_MS; // 1 second
    private String endpoint = DEFAULT_ENDPOINT;
    private boolean useTls;
    @Nullable private Metadata metadata;

    /**
     * Sets the managed chanel to use when communicating with the backend. Takes precedence over
     * {@link #setEndpoint(String)} if both are called.
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
     * Sets the OTLP endpoint to connect to. Optional, defaults to "localhost:55680".
     *
     * @param endpoint endpoint to connect to
     * @return this builder's instance
     */
    public Builder setEndpoint(String endpoint) {
      this.endpoint = endpoint;
      return this;
    }

    /**
     * Sets use or not TLS, default is false. Optional. Applicable only if {@link Builder#endpoint}
     * is set to build channel.
     *
     * @param useTls use TLS or not
     * @return this builder's instance
     */
    public Builder setUseTls(boolean useTls) {
      this.useTls = useTls;
      return this;
    }

    /**
     * Add header to request. Optional. Applicable only if {@link Builder#endpoint} is set to build
     * channel.
     *
     * @param key header key
     * @param value header value
     * @return this builder's instance
     */
    public Builder addHeader(String key, String value) {
      if (metadata == null) {
        metadata = new Metadata();
      }
      metadata.put(Metadata.Key.of(key, ASCII_STRING_MARSHALLER), value);
      return this;
    }

    /**
     * Constructs a new instance of the exporter based on the builder's values.
     *
     * @return a new exporter's instance
     */
    public OtlpGrpcSpanExporter build() {
      if (channel == null) {
        final ManagedChannelBuilder<?> managedChannelBuilder =
            ManagedChannelBuilder.forTarget(endpoint);

        if (useTls) {
          managedChannelBuilder.useTransportSecurity();
        } else {
          managedChannelBuilder.usePlaintext();
        }

        if (metadata != null) {
          managedChannelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(metadata));
        }

        channel = managedChannelBuilder.build();
      }
      return new OtlpGrpcSpanExporter(channel, deadlineMs);
    }

    private Builder() {}

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
      Long value = getLongProperty(KEY_SPAN_TIMEOUT, configMap);
      if (value != null) {
        this.setDeadlineMs(value);
      }
      String endpointValue = getStringProperty(KEY_ENDPOINT, configMap);
      if (endpointValue != null) {
        this.setEndpoint(endpointValue);
      }

      Boolean useTlsValue = getBooleanProperty(KEY_USE_TLS, configMap);
      if (useTlsValue != null) {
        this.setUseTls(useTlsValue);
      }

      String metadataValue = getStringProperty(KEY_METADATA, configMap);
      if (metadataValue != null) {
        for (String keyValueString : Splitter.on(';').split(metadataValue)) {
          final List<String> keyValue = Splitter.on('=').splitToList(keyValueString);
          if (keyValue.size() == 2) {
            addHeader(keyValue.get(0), keyValue.get(1));
          }
        }
      }

      return this;
    }
  }
}
