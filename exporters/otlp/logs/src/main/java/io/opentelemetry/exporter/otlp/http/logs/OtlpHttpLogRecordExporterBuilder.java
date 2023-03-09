/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.okhttp.OkHttpExporterBuilder;
import io.opentelemetry.exporter.internal.otlp.OtlpUserAgent;
import io.opentelemetry.exporter.internal.otlp.logs.LogsRequestMarshaler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509TrustManager;

/** Builder utility for {@link OtlpHttpLogRecordExporter}. */
public final class OtlpHttpLogRecordExporterBuilder {

  private static final String DEFAULT_ENDPOINT = "http://localhost:4318/v1/logs";

  private final OkHttpExporterBuilder<LogsRequestMarshaler> delegate;

  OtlpHttpLogRecordExporterBuilder() {
    delegate = new OkHttpExporterBuilder<>("otlp", "log", DEFAULT_ENDPOINT);
    OtlpUserAgent.addUserAgentHeader(delegate::addHeader);
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of logs. If unset,
   * defaults to {@value OkHttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpLogRecordExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    delegate.setTimeout(timeout, unit);
    return this;
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of logs. If unset,
   * defaults to {@value OkHttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpLogRecordExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  /**
   * Sets the OTLP endpoint to connect to. If unset, defaults to {@value DEFAULT_ENDPOINT}. The
   * endpoint must start with either http:// or https://, and include the full HTTP path.
   */
  public OtlpHttpLogRecordExporterBuilder setEndpoint(String endpoint) {
    requireNonNull(endpoint, "endpoint");
    delegate.setEndpoint(endpoint);
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Currently
   * supported compression methods include "gzip" and "none".
   */
  public OtlpHttpLogRecordExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    checkArgument(
        compressionMethod.equals("gzip") || compressionMethod.equals("none"),
        "Unsupported compression method. Supported compression methods include: gzip, none.");
    delegate.setCompression(compressionMethod);
    return this;
  }

  /** Add header to requests. */
  public OtlpHttpLogRecordExporterBuilder addHeader(String key, String value) {
    delegate.addHeader(key, value);
    return this;
  }

  /**
   * Sets the certificate chain to use for verifying servers when TLS is enabled. The {@code byte[]}
   * should contain an X.509 certificate collection in PEM format. If not set, TLS connections will
   * use the system default trusted certificates.
   */
  public OtlpHttpLogRecordExporterBuilder setTrustedCertificates(byte[] trustedCertificatesPem) {
    delegate.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  /** Sets the preconfigured X509TrustManager to be used to verify servers when TLS is enabled. */
  public OtlpHttpLogRecordExporterBuilder setTrustManager(X509TrustManager trustManager) {
    delegate.setTrustManager(trustManager);
    return this;
  }

  /**
   * Sets ths client key and the certificate chain to use for verifying client when TLS is enabled.
   * The key must be PKCS8, and both must be in PEM format.
   */
  public OtlpHttpLogRecordExporterBuilder setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    delegate.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  /**
   * Sets the "bring-your-own" SSLSocketFactory and X509TrustManager for use with TLS. Users should
   * call this _or_ set raw certificate bytes, but not both.
   */
  public OtlpHttpLogRecordExporterBuilder setSslSocketFactory(
      SSLSocketFactory sslSocketFactory, X509TrustManager trustManager) {
    delegate.setSslSocketFactory(sslSocketFactory, trustManager);
    return this;
  }

  /**
   * Sets the X509KeyManager to use when TLS is enabled. This will replace any existing key manager
   * that has been configured with this method, or with setClientTls().
   */
  public OtlpHttpLogRecordExporterBuilder setKeyManager(X509KeyManager keyManager) {
    delegate.setKeyManager(keyManager);
    return this;
  }

  /**
   * Sets the SSLSocketFactory to use when TLS is enabled. If a preconfigured SSLSocketFactory is
   * provided, a trust manager must also be provided via setTrustManager() or
   * setTrustedCertificates().
   */
  public OtlpHttpLogRecordExporterBuilder setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    delegate.setSslSocketFactory(sslSocketFactory);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to export. If not set, uses
   * {@link GlobalOpenTelemetry#getMeterProvider()}.
   */
  public OtlpHttpLogRecordExporterBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    delegate.setMeterProvider(meterProvider);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpHttpLogRecordExporter build() {
    return new OtlpHttpLogRecordExporter(delegate.build());
  }
}
