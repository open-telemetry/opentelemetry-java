/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.trace;

import static io.grpc.Metadata.ASCII_STRING_MARSHALLER;
import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.Metadata;
import io.grpc.stub.MetadataUtils;
import io.opentelemetry.RetryConfig;
import io.opentelemetry.exporter.otlp.internal.RetryUtil;
import io.opentelemetry.exporter.otlp.internal.grpc.ManagedChannelUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import net.jodah.failsafe.RetryPolicy;

/** Builder utility for this exporter. */
public final class OtlpGrpcSpanExporterBuilder {

  private static final String DEFAULT_ENDPOINT_URL = "http://localhost:4317";
  private static final URI DEFAULT_ENDPOINT = URI.create(DEFAULT_ENDPOINT_URL);
  private static final long DEFAULT_TIMEOUT_SECS = 10;

  @Nullable private ManagedChannel channel;
  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private URI endpoint = DEFAULT_ENDPOINT;
  private boolean compressionEnabled = false;
  @Nullable private Metadata metadata;
  @Nullable private byte[] trustedCertificatesPem;
  private RetryConfig retryConfig;

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
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT_URL}. The
   * endpoint must start with either http:// or https://.
   */
  public OtlpGrpcSpanExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");

    URI uri;
    try {
      uri = new URI(endpoint);
    } catch (URISyntaxException e) {
      throw new IllegalArgumentException("Invalid endpoint, must be a URL: " + endpoint, e);
    }

    if (uri.getScheme() == null
        || (!uri.getScheme().equals("http") && !uri.getScheme().equals("https"))) {
      throw new IllegalArgumentException(
          "Invalid endpoint, must start with http:// or https://: " + uri);
    }

    this.endpoint = uri;
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Currently the
   * only supported compression method is "gzip".
   */
  public OtlpGrpcSpanExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip"),
        "Unsupported compression method. Supported compression methods include: gzip.");
    this.compressionEnabled = true;
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

  /** Set the retry config. */
  public OtlpGrpcSpanExporterBuilder setRetryConfig(RetryConfig retryConfig) {
    requireNonNull(retryConfig, "retryPolicy");
    this.retryConfig = retryConfig;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpGrpcSpanExporter build() {
    ManagedChannel channel = this.channel;
    if (channel == null) {
      final ManagedChannelBuilder<?> managedChannelBuilder =
          ManagedChannelBuilder.forTarget(endpoint.getAuthority());

      if (endpoint.getScheme().equals("https")) {
        managedChannelBuilder.useTransportSecurity();
      } else {
        managedChannelBuilder.usePlaintext();
      }

      if (metadata != null) {
        managedChannelBuilder.intercept(MetadataUtils.newAttachHeadersInterceptor(metadata));
      }

      if (trustedCertificatesPem != null) {
        try {
          ManagedChannelUtil.setTrustedCertificatesPem(
              managedChannelBuilder, trustedCertificatesPem);
        } catch (SSLException e) {
          throw new IllegalStateException(
              "Could not set trusted certificates for gRPC TLS connection, are they valid "
                  + "X.509 in PEM format?",
              e);
        }
      }

      channel = managedChannelBuilder.build();
    }

    RetryPolicy<ExportTraceServiceResponse> retryPolicy =
        retryConfig == null
            ? new RetryPolicy<ExportTraceServiceResponse>().withMaxRetries(0)
            : RetryUtil.toRetryPolicy(retryConfig);

    return new OtlpGrpcSpanExporter(channel, timeoutNanos, compressionEnabled, retryPolicy);
  }

  OtlpGrpcSpanExporterBuilder() {}
}
