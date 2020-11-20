/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import com.google.common.base.Splitter;
import com.google.common.util.concurrent.FutureCallback;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.MoreExecutors;
import io.grpc.ConnectivityState;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Labels;
import io.opentelemetry.api.metrics.LongCounter;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceRequest;
import io.opentelemetry.proto.collector.trace.v1.ExportTraceServiceResponse;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc;
import io.opentelemetry.proto.collector.trace.v1.TraceServiceGrpc.TraceServiceFutureStub;
import io.opentelemetry.sdk.common.CompletableResultCode;
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
 *   <li>{@code otel.exporter.otlp.span.timeout}: to set the max waiting time allowed to send each
 *       span batch.
 *   <li>{@code otel.exporter.otlp.span.endpoint}: to set the endpoint to connect to.
 *   <li>{@code otel.exporter.otlp.span.insecure}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code otel.exporter.otlp.span.headers}: the headers associated with the requests.
 * </ul>
 *
 * <p>For environment variables, {@link OtlpGrpcSpanExporter} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_TIMEOUT}: to set the max waiting time allowed to send each
 *       span batch.
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_ENDPOINT}: to set the endpoint to connect to.
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_INSECURE}: whether to enable client transport security for
 *       the connection.
 *   <li>{@code OTEL_EXPORTER_OTLP_SPAN_HEADERS}: the headers associated with the requests.
 * </ul>
 *
 * <p>In both cases, if a property is missing, the name without "span" is used to resolve the value.
 */
@ThreadSafe
public final class OtlpGrpcSpanExporter implements SpanExporter {

  public static final String DEFAULT_ENDPOINT = "localhost:4317";
  public static final long DEFAULT_DEADLINE_MS = TimeUnit.SECONDS.toMillis(1);

  private static final Logger logger = Logger.getLogger(OtlpGrpcSpanExporter.class.getName());
  private static final boolean DEFAULT_USE_TLS = false;
  private static final String EXPORTER_NAME = OtlpGrpcSpanExporter.class.getSimpleName();

  private final TraceServiceFutureStub traceService;
  private final ManagedChannel managedChannel;
  private final long deadlineMs;

  private final LongCounter spansSeen =
      OpenTelemetry.getGlobalMeter("io.opentelemetry.exporters.otlp")
          .longCounterBuilder("spansSeenByExporter")
          .build();

  private final LongCounter spansExported =
      OpenTelemetry.getGlobalMeter("io.opentelemetry.exporters.otlp")
          .longCounterBuilder("spansExportedByExporter")
          .build();

  /**
   * Creates a new OTLP gRPC Span Reporter with the given name, using the given channel.
   *
   * @param channel the channel to use when communicating with the OpenTelemetry Collector.
   * @param deadlineMs max waiting time for the collector to process each span batch. When set to 0
   *     or to a negative value, the exporter will wait indefinitely.
   */
  private OtlpGrpcSpanExporter(ManagedChannel channel, long deadlineMs) {
    this.managedChannel = channel;
    this.deadlineMs = deadlineMs;
    this.traceService = TraceServiceGrpc.newFutureStub(channel);
  }

  /**
   * Submits all the given spans in a single batch to the OpenTelemetry collector.
   *
   * @param spans the list of sampled Spans to be exported.
   * @return the result of the operation
   */
  @Override
  public CompletableResultCode export(Collection<SpanData> spans) {
    spansSeen.add(spans.size(), Labels.of("exporter", OtlpGrpcSpanExporter.class.getSimpleName()));
    ExportTraceServiceRequest exportTraceServiceRequest =
        ExportTraceServiceRequest.newBuilder()
            .addAllResourceSpans(SpanAdapter.toProtoResourceSpans(spans))
            .build();

    final CompletableResultCode result = new CompletableResultCode();

    TraceServiceFutureStub exporter;
    if (deadlineMs > 0) {
      exporter = traceService.withDeadlineAfter(deadlineMs, TimeUnit.MILLISECONDS);
    } else {
      exporter = traceService;
    }

    Futures.addCallback(
        exporter.export(exportTraceServiceRequest),
        new FutureCallback<ExportTraceServiceResponse>() {
          @Override
          public void onSuccess(@Nullable ExportTraceServiceResponse response) {
            spansExported.add(
                spans.size(),
                Labels.of(
                    "exporter", OtlpGrpcSpanExporter.class.getSimpleName(), "success", "true"));
            result.succeed();
          }

          @Override
          public void onFailure(Throwable t) {
            spansExported.add(
                spans.size(), Labels.of("exporter", EXPORTER_NAME, "success", "false"));
            logger.log(Level.WARNING, "Failed to export spans. Error message: " + t.getMessage());
            logger.log(Level.FINEST, "Failed to export spans. Details follow: " + t);
            result.fail();
          }
        },
        MoreExecutors.directExecutor());
    return result;
  }

  /**
   * The OTLP exporter does not batch spans, so this method will immediately return with success.
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
   * Returns a new {@link OtlpGrpcSpanExporter} reading the configuration values from the
   * environment and from system properties. System properties override values defined in the
   * environment. If a configuration value is missing, it uses the default value.
   *
   * @return a new {@link OtlpGrpcSpanExporter} instance.
   */
  public static OtlpGrpcSpanExporter getDefault() {
    return builder().readEnvironmentVariables().readSystemProperties().build();
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

    private static final String KEY_TIMEOUT = "otel.exporter.otlp.span.timeout";
    private static final String KEY_ENDPOINT = "otel.exporter.otlp.span.endpoint";
    private static final String KEY_INSECURE = "otel.exporter.otlp.span.insecure";
    private static final String KEY_HEADERS = "otel.exporter.otlp.span.headers";
    private ManagedChannel channel;
    private long deadlineMs = DEFAULT_DEADLINE_MS; // 1 second
    private String endpoint = DEFAULT_ENDPOINT;
    private boolean useTls = DEFAULT_USE_TLS;
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
     * Sets the OTLP endpoint to connect to. Optional, defaults to "localhost:4317".
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

      Long value = getLongProperty(KEY_TIMEOUT, configMap);
      if (value == null) {
        value = getLongProperty(CommonProperties.KEY_TIMEOUT, configMap);
      }
      if (value != null) {
        this.setDeadlineMs(value);
      }

      String endpointValue = getStringProperty(KEY_ENDPOINT, configMap);
      if (endpointValue == null) {
        endpointValue = getStringProperty(CommonProperties.KEY_ENDPOINT, configMap);
      }
      if (endpointValue != null) {
        this.setEndpoint(endpointValue);
      }

      Boolean insecure = getBooleanProperty(KEY_INSECURE, configMap);
      if (insecure == null) {
        insecure = getBooleanProperty(CommonProperties.KEY_INSECURE, configMap);
      }
      if (insecure != null) {
        this.setUseTls(!insecure);
      }

      String metadataValue = getStringProperty(KEY_HEADERS, configMap);
      if (metadataValue == null) {
        metadataValue = getStringProperty(CommonProperties.KEY_HEADERS, configMap);
      }
      if (metadataValue != null) {
        for (String keyValueString : Splitter.on(';').split(metadataValue)) {
          final List<String> keyValue =
              Splitter.on('=')
                  .limit(2)
                  .trimResults()
                  .omitEmptyStrings()
                  .splitToList(keyValueString);
          if (keyValue.size() == 2) {
            addHeader(keyValue.get(0), keyValue.get(1));
          }
        }
      }

      return this;
    }
  }
}
