/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.compression.CompressorProvider;
import io.opentelemetry.exporter.internal.compression.CompressorUtil;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.internal.otlp.traces.TraceRequestMarshaler;
import io.opentelemetry.exporter.otlp.internal.OtlpUserAgent;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Builder utility for {@link OtlpHttpSpanExporter}.
 *
 * @since 1.5.0
 */
public final class OtlpHttpSpanExporterBuilder {

  private static final String DEFAULT_ENDPOINT = "http://localhost:4318/v1/traces";

  private final HttpExporterBuilder<TraceRequestMarshaler> delegate;

  OtlpHttpSpanExporterBuilder(HttpExporterBuilder<TraceRequestMarshaler> delegate) {
    this.delegate = delegate;
    OtlpUserAgent.addUserAgentHeader(delegate::addConstantHeaders);
  }

  OtlpHttpSpanExporterBuilder() {
    this(new HttpExporterBuilder<>("otlp", "span", DEFAULT_ENDPOINT));
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of spans. If
   * unset, defaults to {@value HttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpSpanExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of spans. If
   * unset, defaults to {@value HttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpSpanExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the maximum time to wait for new connections to be established. If unset, defaults to
   * {@value HttpExporterBuilder#DEFAULT_CONNECT_TIMEOUT_SECS}s.
   *
   * @since 1.33.0
   */
  public OtlpHttpSpanExporterBuilder setConnectTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setConnectTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for new connections to be established. If unset, defaults to
   * {@value HttpExporterBuilder#DEFAULT_CONNECT_TIMEOUT_SECS}s.
   *
   * @since 1.33.0
   */
  public OtlpHttpSpanExporterBuilder setConnectTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setConnectTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT}. The
   * endpoint must start with either http:// or https://, and include the full HTTP path.
   */
  public OtlpHttpSpanExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    delegate.setEndpoint(endpoint);
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Compression
   * method "gzip" and "none" are supported out of the box. Support for additional compression
   * methods is available by implementing {@link Compressor} and {@link CompressorProvider}.
   */
  public OtlpHttpSpanExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    Compressor compressor = CompressorUtil.validateAndResolveCompressor(compressionMethod);
    delegate.setCompression(compressor);
    return this;
  }

  /**
   * Add a constant header to requests. If the {@code key} collides with another constant header
   * name or a one from {@link #setHeaders(Supplier)}, the values from both are included.
   */
  public OtlpHttpSpanExporterBuilder addHeader(String key, String value) {
    delegate.addConstantHeaders(key, value);
    return this;
  }

  /**
   * Set the supplier of headers to add to requests. If a key from the map collides with a constant
   * from {@link #addHeader(String, String)}, the values from both are included.
   *
   * @since 1.33.0
   */
  public OtlpHttpSpanExporterBuilder setHeaders(Supplier<Map<String, String>> headerSupplier) {
    delegate.setHeadersSupplier(headerSupplier);
    return this;
  }

  /**
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpHttpSpanExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    delegate.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  /**
   * Sets ths client key and the certificate chain to use for verifying client when TLS is enabled.
   * The key must be PKCS8, and both must be in PEM format.
   */
  public OtlpHttpSpanExporterBuilder setClientTls(byte[] privateKeyPem, byte[] certificatePem) {
    delegate.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  /**
   * Sets the "bring-your-own" SSLContext for use with TLS. Users should call this _or_ set raw
   * certificate bytes, but not both.
   *
   * @since 1.26.0
   */
  public OtlpHttpSpanExporterBuilder setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    delegate.setSslContext(sslContext, trustManager);
    return this;
  }

  /**
   * Ses the retry policy. Retry is disabled by default.
   *
   * @since 1.28.0
   */
  public OtlpHttpSpanExporterBuilder setRetryPolicy(RetryPolicy retryPolicy) {
    requireNonNull(retryPolicy, "retryPolicy");
    delegate.setRetryPolicy(retryPolicy);
    return this;
  }

  /**
   * Sets the proxy options. Proxying is disabled by default.
   *
   * @since 1.36.0
   */
  public OtlpHttpSpanExporterBuilder setProxy(ProxyOptions proxyOptions) {
    requireNonNull(proxyOptions, "proxyOptions");
    delegate.setProxyOptions(proxyOptions);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to export. If not set, uses
   * {@link GlobalOpenTelemetry#getMeterProvider()}.
   */
  public OtlpHttpSpanExporterBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    setMeterProvider(() -> meterProvider);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} supplier to use to collect metrics related to export. If not
   * set, uses {@link GlobalOpenTelemetry#getMeterProvider()}.
   *
   * @since 1.32.0
   */
  public OtlpHttpSpanExporterBuilder setMeterProvider(
      Supplier<MeterProvider> meterProviderSupplier) {
    requireNonNull(meterProviderSupplier, "meterProviderSupplier");
    delegate.setMeterProvider(meterProviderSupplier);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpHttpSpanExporter build() {
    return new OtlpHttpSpanExporter(delegate, delegate.build());
  }
}
