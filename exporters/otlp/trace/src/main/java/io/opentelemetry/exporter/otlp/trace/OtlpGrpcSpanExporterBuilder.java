/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import com.google.common.base.Splitter;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.netty.GrpcSslContexts;
import io.grpc.netty.NettyChannelBuilder;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.extension.otproto.CommonProperties;
import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;

/** Builder utility for this exporter. */
public final class OtlpGrpcSpanExporterBuilder extends ConfigBuilder<OtlpGrpcSpanExporterBuilder> {

  private static final String DEFAULT_ENDPOINT = "localhost:4317";
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  private static final String KEY_TIMEOUT = "otel.exporter.otlp.span.timeout";
  private static final String KEY_ENDPOINT = "otel.exporter.otlp.span.endpoint";
  private static final String KEY_INSECURE = "otel.exporter.otlp.span.insecure";
  private static final String KEY_HEADERS = "otel.exporter.otlp.span.headers";

  private ManagedChannel channel;
  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private String endpoint = DEFAULT_ENDPOINT;
  private boolean useTls = false;
  @Nullable private Metadata metadata;
  @Nullable private byte[] trustedCertificatesPem;

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
   * Sets the maximum time to wait for the collector to process an exported batch of spans. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpGrpcSpanExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of spans. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpGrpcSpanExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the max waiting time for the collector to process each span batch. Optional.
   *
   * @param deadlineMs the max waiting time
   * @return this builder's instance
   * @deprecated Use {@link #setTimeout(long, TimeUnit)}
   */
  @Deprecated
  public OtlpGrpcSpanExporterBuilder setDeadlineMs(long deadlineMs) {
    return setTimeout(Duration.ofMillis(deadlineMs));
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
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpGrpcSpanExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
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

      if (trustedCertificatesPem != null) {
        // gRPC does not abstract TLS configuration so we need to check the implementation and act
        // accordingly.
        if (managedChannelBuilder
            .getClass()
            .getName()
            .equals("io.grpc.netty.NettyChannelBuilder")) {
          NettyChannelBuilder nettyBuilder = (NettyChannelBuilder) managedChannelBuilder;
          try {
            nettyBuilder.sslContext(
                GrpcSslContexts.forClient()
                    .trustManager(new ByteArrayInputStream(trustedCertificatesPem))
                    .build());
          } catch (IllegalArgumentException | SSLException e) {
            throw new IllegalStateException(
                "Could not set trusted certificates for gRPC TLS connection, are they valid "
                    + "X.509 in PEM format?",
                e);
          }
        } else if (managedChannelBuilder
            .getClass()
            .getName()
            .equals("io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder")) {
          io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder nettyBuilder =
              (io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder) managedChannelBuilder;
          try {
            nettyBuilder.sslContext(
                io.grpc.netty.shaded.io.grpc.netty.GrpcSslContexts.forClient()
                    .trustManager(new ByteArrayInputStream(trustedCertificatesPem))
                    .build());
          } catch (IllegalArgumentException | SSLException e) {
            throw new IllegalStateException(
                "Could not set trusted certificates for gRPC TLS connection, are they valid "
                    + "X.509 in PEM format?",
                e);
          }
        } else {
          throw new IllegalStateException(
              "TLS cerificate configuration only supported with Netty. "
                  + "If you need to configure a certificate, switch to grpc-netty or "
                  + "grpc-netty-shaded.");
        }
        // TODO(anuraaga): Support okhttp.
      }

      channel = managedChannelBuilder.build();
    }
    return new OtlpGrpcSpanExporter(channel, timeoutNanos);
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
      this.setTimeout(Duration.ofMillis(value));
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
      for (String keyValueString : Splitter.on(',').split(metadataValue)) {
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
