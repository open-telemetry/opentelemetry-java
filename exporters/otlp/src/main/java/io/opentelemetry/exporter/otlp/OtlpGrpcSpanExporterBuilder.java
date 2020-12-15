/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;

import com.google.common.base.Splitter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

/** Builder utility for this exporter. */
public class OtlpGrpcSpanExporterBuilder extends ConfigBuilder<OtlpGrpcSpanExporterBuilder> {

  private static final String KEY_TIMEOUT = "otel.exporter.otlp.span.timeout";
  private static final String KEY_ENDPOINT = "otel.exporter.otlp.span.endpoint";
  private static final String KEY_INSECURE = "otel.exporter.otlp.span.insecure";
  private static final String KEY_HEADERS = "otel.exporter.otlp.span.headers";

  private ManagedChannel channel;
  private long deadlineMs = OtlpGrpcSpanExporter.DEFAULT_DEADLINE_MS; // 10 seconds
  private String endpoint = OtlpGrpcSpanExporter.DEFAULT_ENDPOINT;
  private boolean useTls = false;
  @Nullable private Metadata metadata;

  /**
   * Sets the managed chanel to use when communicating with the backend. Takes precedence over
   * {@link #setEndpoint(String)} if both are called.
   *
   * @param channel the channel to use
   * @return this builder's instance
   */
  public OtlpGrpcSpanExporterBuilder setChannel(ManagedChannel channel) {
    this.channel = channel;
    return this;
  }

  /**
   * Sets the max waiting time for the collector to process each span batch. Optional.
   *
   * @param deadlineMs the max waiting time
   * @return this builder's instance
   */
  public OtlpGrpcSpanExporterBuilder setDeadlineMs(long deadlineMs) {
    this.deadlineMs = deadlineMs;
    return this;
  }

  /**
   * Sets the OTLP endpoint to connect to. Optional, defaults to "localhost:4317".
   *
   * @param endpoint endpoint to connect to
   * @return this builder's instance
   */
  public OtlpGrpcSpanExporterBuilder setEndpoint(String endpoint) {
    this.endpoint = endpoint;
    return this;
  }

  /**
   * Sets use or not TLS, default is false. Optional. Applicable only if {@link
   * OtlpGrpcSpanExporterBuilder#endpoint} is set to build channel.
   *
   * @param useTls use TLS or not
   * @return this builder's instance
   */
  public OtlpGrpcSpanExporterBuilder setUseTls(boolean useTls) {
    this.useTls = useTls;
    return this;
  }

  /**
   * Add header to request. Optional. Applicable only if {@link
   * OtlpGrpcSpanExporterBuilder#endpoint} is set to build channel.
   *
   * @param key header key
   * @param value header value
   * @return this builder's instance
   */
  public OtlpGrpcSpanExporterBuilder addHeader(String key, String value) {
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

  OtlpGrpcSpanExporterBuilder() {}

  /**
   * Sets the configuration values from the given configuration map for only the available keys.
   *
   * @param configMap {@link Map} holding the configuration values.
   * @return this.
   */
  @Override
  protected OtlpGrpcSpanExporterBuilder fromConfigMap(
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
            Splitter.on('=').limit(2).trimResults().omitEmptyStrings().splitToList(keyValueString);
        if (keyValue.size() == 2) {
          addHeader(keyValue.get(0), keyValue.get(1));
        }
      }
    }

    return this;
  }
}
