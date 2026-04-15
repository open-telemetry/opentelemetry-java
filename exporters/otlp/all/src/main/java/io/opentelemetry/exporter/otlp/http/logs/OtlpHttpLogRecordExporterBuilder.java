/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.internal.http.HttpExporterBuilder;
import io.opentelemetry.exporter.otlp.internal.OtlpUserAgent;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.Compressor;
import io.opentelemetry.sdk.common.export.MemoryMode;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.common.internal.StandardComponentId;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Builder utility for {@link OtlpHttpLogRecordExporter}.
 *
 * @since 1.27.0
 */
public final class OtlpHttpLogRecordExporterBuilder {

  private static final String DEFAULT_ENDPOINT = "http://localhost:4318/v1/logs";
  private static final MemoryMode DEFAULT_MEMORY_MODE = MemoryMode.REUSABLE_DATA;

  private final HttpExporterBuilder delegate;
  private MemoryMode memoryMode;

  OtlpHttpLogRecordExporterBuilder(HttpExporterBuilder delegate, MemoryMode memoryMode) {
    this.delegate = delegate;
    this.memoryMode = memoryMode;
    OtlpUserAgent.addUserAgentHeader(delegate::addConstantHeaders);
  }

  OtlpHttpLogRecordExporterBuilder() {
    this(
        new HttpExporterBuilder(
            StandardComponentId.ExporterType.OTLP_HTTP_LOG_EXPORTER, DEFAULT_ENDPOINT),
        DEFAULT_MEMORY_MODE);
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of logs. If unset,
   * defaults to {@value HttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpLogRecordExporterBuilder setTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    return setTimeout(Duration.ofNanos(unit.toNanos(timeout)));
  }

  /**
   * Sets the maximum time to wait for the collector to process an exported batch of logs. If unset,
   * defaults to {@value HttpExporterBuilder#DEFAULT_TIMEOUT_SECS}s.
   */
  public OtlpHttpLogRecordExporterBuilder setTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    delegate.setTimeout(timeout);
    return this;
  }

  /**
   * Sets the maximum time to wait for new connections to be established. If unset, defaults to
   * {@value HttpExporterBuilder#DEFAULT_CONNECT_TIMEOUT_SECS}s.
   *
   * @since 1.33.0
   */
  public OtlpHttpLogRecordExporterBuilder setConnectTimeout(long timeout, TimeUnit unit) {
    requireNonNull(unit, "unit");
    checkArgument(timeout >= 0, "timeout must be non-negative");
    return setConnectTimeout(Duration.ofNanos(unit.toNanos(timeout)));
  }

  /**
   * Sets the maximum time to wait for new connections to be established. If unset, defaults to
   * {@value HttpExporterBuilder#DEFAULT_CONNECT_TIMEOUT_SECS}s.
   *
   * @since 1.33.0
   */
  public OtlpHttpLogRecordExporterBuilder setConnectTimeout(Duration timeout) {
    requireNonNull(timeout, "timeout");
    delegate.setConnectTimeout(timeout);
    return this;
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
   * Sets the method used to compress payloads. If unset, compression is disabled. Compression
   * method "gzip" and "none" are supported out of the box. Additional compression methods can be
   * supported by providing custom {@link Compressor} implementations via the service loader.
   */
  public OtlpHttpLogRecordExporterBuilder setCompression(String compressionMethod) {
    requireNonNull(compressionMethod, "compressionMethod");
    delegate.setCompression(compressionMethod);
    return this;
  }

  /**
   * Add a constant header to requests. If the {@code key} collides with another constant header
   * name or a one from {@link #setHeaders(Supplier)}, the values from both are included.
   */
  public OtlpHttpLogRecordExporterBuilder addHeader(String key, String value) {
    delegate.addConstantHeaders(key, value);
    return this;
  }

  /**
   * Set the supplier of headers to add to requests. If a key from the map collides with a constant
   * from {@link #addHeader(String, String)}, the values from both are included.
   *
   * @since 1.33.0
   */
  public OtlpHttpLogRecordExporterBuilder setHeaders(Supplier<Map<String, String>> headerSupplier) {
    delegate.setHeadersSupplier(headerSupplier);
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
   * Sets the "bring-your-own" SSLContext for use with TLS. Users should call this _or_ set raw
   * certificate bytes, but not both.
   */
  public OtlpHttpLogRecordExporterBuilder setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    delegate.setSslContext(sslContext, trustManager);
    return this;
  }

  /**
   * Set the retry policy, or {@code null} to disable retry. Retry policy is {@link
   * RetryPolicy#getDefault()} by default
   *
   * @since 1.28.0
   */
  public OtlpHttpLogRecordExporterBuilder setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    delegate.setRetryPolicy(retryPolicy);
    return this;
  }

  /**
   * Sets the proxy options. Proxying is disabled by default.
   *
   * @since 1.36.0
   */
  public OtlpHttpLogRecordExporterBuilder setProxyOptions(ProxyOptions proxyOptions) {
    requireNonNull(proxyOptions, "proxyOptions");
    delegate.setProxyOptions(proxyOptions);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} to use to collect metrics related to export. If not set, uses
   * {@link GlobalOpenTelemetry#getMeterProvider()}.
   */
  public OtlpHttpLogRecordExporterBuilder setMeterProvider(MeterProvider meterProvider) {
    requireNonNull(meterProvider, "meterProvider");
    setMeterProvider(() -> meterProvider);
    return this;
  }

  /**
   * Sets the {@link MeterProvider} supplier used to collect metrics related to export. If not set,
   * uses {@link GlobalOpenTelemetry#getMeterProvider()}.
   *
   * @since 1.32.0
   */
  public OtlpHttpLogRecordExporterBuilder setMeterProvider(
      Supplier<MeterProvider> meterProviderSupplier) {
    requireNonNull(meterProviderSupplier, "meterProviderSupplier");
    delegate.setMeterProvider(meterProviderSupplier);
    return this;
  }

  /**
   * Sets the {@link InternalTelemetryVersion} defining which self-monitoring metrics this exporter
   * collects.
   *
   * @since 1.51.0
   */
  public OtlpHttpLogRecordExporterBuilder setInternalTelemetryVersion(
      InternalTelemetryVersion schemaVersion) {
    requireNonNull(schemaVersion, "schemaVersion");
    delegate.setInternalTelemetryVersion(schemaVersion);
    return this;
  }

  /**
   * Set the {@link MemoryMode}. If unset, defaults to {@link #DEFAULT_MEMORY_MODE}.
   *
   * <p>When memory mode is {@link MemoryMode#REUSABLE_DATA}, serialization is optimized to reduce
   * memory allocation.
   *
   * @since 1.39.0
   */
  public OtlpHttpLogRecordExporterBuilder setMemoryMode(MemoryMode memoryMode) {
    requireNonNull(memoryMode, "memoryMode");
    this.memoryMode = memoryMode;
    return this;
  }

  /**
   * Set the {@link ClassLoader} used to load the sender API. Variant of {@link
   * #setComponentLoader(ComponentLoader)}.
   *
   * @since 1.48.0
   */
  public OtlpHttpLogRecordExporterBuilder setServiceClassLoader(ClassLoader serviceClassLoader) {
    requireNonNull(serviceClassLoader, "serviceClassLoader");
    return setComponentLoader(ComponentLoader.forClassLoader(serviceClassLoader));
  }

  /** Set the {@link ComponentLoader} used to load the sender API. */
  public OtlpHttpLogRecordExporterBuilder setComponentLoader(ComponentLoader componentLoader) {
    requireNonNull(componentLoader, "componentLoader");
    delegate.setComponentLoader(componentLoader);
    return this;
  }

  /**
   * Set the {@link ExecutorService} used to execute requests.
   *
   * <p>NOTE: By calling this method, you are opting into managing the lifecycle of the {@code
   * executorService}. {@link ExecutorService#shutdown()} will NOT be called when this exporter is
   * shutdown.
   *
   * @since 1.49.0
   */
  public OtlpHttpLogRecordExporterBuilder setExecutorService(ExecutorService executorService) {
    requireNonNull(executorService, "executorService");
    delegate.setExecutorService(executorService);
    return this;
  }

  /**
   * Constructs a new instance of the exporter based on the builder's values.
   *
   * @return a new exporter's instance
   */
  public OtlpHttpLogRecordExporter build() {
    return new OtlpHttpLogRecordExporter(delegate, delegate.build(), memoryMode);
  }
}
