/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.metrics;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.compression.CompressorProvider;
import io.opentelemetry.exporter.internal.compression.CompressorUtil;
import io.opentelemetry.exporter.internal.grpc.GrpcExporterBuilder;
import io.opentelemetry.exporter.internal.otlp.metrics.MetricsRequestMarshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpUserAgent;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.metrics.InstrumentType;
import io.opentelemetry.sdk.metrics.export.AggregationTemporalitySelector;
import io.opentelemetry.sdk.metrics.export.DefaultAggregationSelector;
import io.opentelemetry.sdk.metrics.export.MetricExporter;
import java.net.URI;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Builder utility for this exporter.
 *
 * @since 1.14.0
 */
public final class OtlpGrpcMetricExporterBuilder {

  private static final String GRPC_SERVICE_NAME =
      "opentelemetry.proto.collector.metrics.v1.MetricsService";
  // Visible for testing
  static final String GRPC_ENDPOINT_PATH = "/" + GRPC_SERVICE_NAME + "/Export";

  private static final String DEFAULT_ENDPOINT_URL = "http://localhost:4317";
  private static final URI DEFAULT_ENDPOINT = URI.create(DEFAULT_ENDPOINT_URL);
  private static final long DEFAULT_TIMEOUT_SECS = 10;
  private static final AggregationTemporalitySelector DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR =
      AggregationTemporalitySelector.alwaysCumulative();
  private static final MemoryMode DEFAULT_MEMORY_MODE = MemoryMode.IMMUTABLE_DATA;

  // Visible for testing
  final GrpcExporterBuilder<MetricsRequestMarshaler> delegate;

  private AggregationTemporalitySelector aggregationTemporalitySelector =
      DEFAULT_AGGREGATION_TEMPORALITY_SELECTOR;

  private DefaultAggregationSelector defaultAggregationSelector =
      DefaultAggregationSelector.getDefault();
  private MemoryMode memoryMode;

  OtlpGrpcMetricExporterBuilder(
      GrpcExporterBuilder<MetricsRequestMarshaler> delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
    delegate.setMeterProvider(MeterProvider::noop);
    OtlpUserAgent.addUserAgentHeader(delegate::addConstantHeader);
  }

  OtlpGrpcMetricExporterBuilder() {
    this(
        new GrpcExporterBuilder<>(
            "otlp",
            "metric",
            DEFAULT_TIMEOUT_SECS,
            DEFAULT_ENDPOINT,
            () -> MarshalerMetricsServiceGrpc::newFutureStub,
            GRPC_ENDPOINT_PATH),
        DEFAULT_MEMORY_MODE);
  }

  /**
   * Sets the managed chanel to use when communicating with the backend. Takes precedence over
   * {@link #setEndpoint(String)} if both are called.
   *
   * <p>Note: calling this overrides the spec compliant {@code User-Agent} header. To ensure spec
   * compliance, set {@link io.grpc.ManagedChannelBuilder#userAgent(String)} to {@link
   * OtlpUserAgent#getUserAgent()} when building the channel.
   *
   * @param channel the channel to use
   * @return this builder's instance
   * @deprecated Use {@link #setEndpoint(String)}. If you have a use case not satisfied by the
   *     methods on this builder, please file an issue to let us know what it is.
   */
  @Deprecated
  public OtlpGrpcMetricExporterBuilder setChannel(ManagedChannel channel) {
    delegate.setChannel(channel);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of metrics. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpGrpcMetricExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of metrics. If
   * unset, defaults to {@value DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpGrpcMetricExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    delegate.setTimeout(timeout);
    return this;
  }

  /**
   * Sets the maximum time to wait for new connections to be established. If unset, defaults to
   * {@value GrpcExporterBuilder#DEFAULT_CONNECT_TIMEOUT_SECS}s.
   *
   * @since 1.36.0
   */
  public OtlpGrpcMetricExporterBuilder setConnectTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setConnectTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for new connections to be established. If unset, defaults to
   * {@value GrpcExporterBuilder#DEFAULT_CONNECT_TIMEOUT_SECS}s.
   *
   * @since 1.36.0
   */
  public OtlpGrpcMetricExporterBuilder setConnectTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setConnectTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT_URL}. The
   * endpoint must start with either http:// or https://.
   */
  public OtlpGrpcMetricExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    delegate.setEndpoint(endpoint);
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Compression
   * method "gzip" and "none" are supported out of the box. Support for additional compression
   * methods is available by implementing {@link Compressor} and {@link CompressorProvider}.
   */
  public OtlpGrpcMetricExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    Compressor compressor = CompressorUtil.validateAndResolveCompressor(compressionMethod);
    delegate.setCompression(compressor);
    return this;
  }

  /**
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpGrpcMetricExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    delegate.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  /**
   * Sets ths client key and the certificate chain to use for verifying client when TLS is enabled.
   * The key must be PKCS8, and both must be in PEM format.
   */
  public OtlpGrpcMetricExporterBuilder setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    delegate.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  /**
   * Sets the "bring-your-own" SSLContext for use with TLS. Users should call this _or_ set raw
   * certificate bytes, but not both.
   *
   * @since 1.26.0
   */
  public OtlpGrpcMetricExporterBuilder setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    delegate.setSslContext(sslContext, trustManager);
    return this;
  }

  /**
   * Add a constant header to requests. If the {@code key} collides with another constant header
   * name or a one from {@link #setHeaders(Supplier)}, the values from both are included. Applicable
   * only if {@link OtlpGrpcMetricExporterBuilder#setChannel(ManagedChannel)} is not used to set
   * channel.
   *
   * @param key header key
   * @param value header value
   * @return this builder's instance
   */
  public OtlpGrpcMetricExporterBuilder addHeader(String key, String value) {
    delegate.addConstantHeader(key, value);
    return this;
  }

  /**
   * Set the supplier of headers to add to requests. If a key from the map collides with a constant
   * from {@link #addHeader(String, String)}, the values from both are included. Applicable only if
   * {@link OtlpGrpcMetricExporterBuilder#setChannel(ManagedChannel)} is not used to set channel.
   *
   * @since 1.33.0
   */
  public OtlpGrpcMetricExporterBuilder setHeaders(Supplier<Map<String, String>> headerSupplier) {
    delegate.setHeadersSupplier(headerSupplier);
    return this;
  }

  /**
   * Set the {@link AggregationTemporalitySelector} used for {@link
   * MetricExporter#getAggregationTemporality(InstrumentType)}.
   *
   * <p>If unset, defaults to {@link AggregationTemporalitySelector#alwaysCumulative()}.
   *
   * <p>{@link AggregationTemporalitySelector#deltaPreferred()} is a common configuration for delta
   * backends.
   */
  public OtlpGrpcMetricExporterBuilder setAggregationTemporalitySelector(
      AggregationTemporalitySelector aggregationTemporalitySelector) {
    requireNonNull(aggregationTemporalitySelector, "aggregationTemporalitySelector");
    this.aggregationTemporalitySelector = aggregationTemporalitySelector;
    return this;
  }

  /**
   * Set the {@link DefaultAggregationSelector} used for {@link
   * MetricExporter#getDefaultAggregation(InstrumentType)}.
   *
   * <p>If unset, defaults to {@link DefaultAggregationSelector#getDefault()}.
   *
   * @since 1.16.0
   */
  public OtlpGrpcMetricExporterBuilder setDefaultAggregationSelector(
      DefaultAggregationSelector defaultAggregationSelector) {
    requireNonNull(defaultAggregationSelector, "defaultAggregationSelector");
    this.defaultAggregationSelector = defaultAggregationSelector;
    return this;
  }

  /**
   * Ses the retry policy. Retry is disabled by default.
   *
   * @since 1.28.0
   */
  public OtlpGrpcMetricExporterBuilder setRetryPolicy(RetryPolicy retryPolicy) {
    requireNonNull(retryPolicy, "retryPolicy");
    delegate.setRetryPolicy(retryPolicy);
    return this;
  }

  /** Set the {@link MemoryMode}. */
  OtlpGrpcMetricExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    requireNonNull(memoryMode, "memoryMode");
    this.memoryMode = memoryMode;
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpGrpcMetricExporter build() {
    return new OtlpGrpcMetricExporter(
        delegate,
        delegate.build(),
        aggregationTemporalitySelector,
        defaultAggregationSelector,
        memoryMode);
  }
}
