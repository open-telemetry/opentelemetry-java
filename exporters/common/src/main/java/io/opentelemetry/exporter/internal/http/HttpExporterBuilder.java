/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;

/**
 * A builder for {@link HttpExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class HttpExporterBuilder<T extends Marshaler> {
  public static final long DEFAULT_TIMEOUT_SECS = 10;

  private final String exporterName;
  private final String type;

  private String endpoint;

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private boolean compressionEnabled = false;
  private boolean exportAsJson = false;
  @Nullable private Map<String, String> headers;

  private final TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();
  @Nullable private RetryPolicy retryPolicy;
  private Supplier<MeterProvider> meterProviderSupplier = GlobalOpenTelemetry::getMeterProvider;
  @Nullable private Authenticator authenticator;

  public HttpExporterBuilder(String exporterName, String type, String defaultEndpoint) {
    this.exporterName = exporterName;
    this.type = type;

    endpoint = defaultEndpoint;
  }

  public HttpExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  public HttpExporterBuilder<T> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  public HttpExporterBuilder<T> setEndpoint(String endpoint) {
    URI uri = ExporterBuilderUtil.validateEndpoint(endpoint);
    this.endpoint = uri.toString();
    return this;
  }

  public HttpExporterBuilder<T> setCompression(String compressionMethod) {
    this.compressionEnabled = compressionMethod.equals("gzip");
    return this;
  }

  public HttpExporterBuilder<T> addHeader(String key, String value) {
    if (headers == null) {
      headers = new HashMap<>();
    }
    headers.put(key, value);
    return this;
  }

  public HttpExporterBuilder<T> setAuthenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  public HttpExporterBuilder<T> configureTrustManager(byte[] trustedCertificatesPem) {
    tlsConfigHelper.createTrustManager(trustedCertificatesPem);
    return this;
  }

  public HttpExporterBuilder<T> setTrustManager(X509TrustManager trustManager) {
    tlsConfigHelper.setTrustManager(trustManager);
    return this;
  }

  public HttpExporterBuilder<T> configureKeyManager(byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.createKeyManager(privateKeyPem, certificatePem);
    return this;
  }

  public HttpExporterBuilder<T> setKeyManager(X509KeyManager keyManager) {
    tlsConfigHelper.setKeyManager(keyManager);
    return this;
  }

  public HttpExporterBuilder<T> setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    tlsConfigHelper.setSslSocketFactory(sslSocketFactory);
    return this;
  }

  public HttpExporterBuilder<T> setMeterProvider(MeterProvider meterProvider) {
    this.meterProviderSupplier = () -> meterProvider;
    return this;
  }

  public HttpExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public HttpExporterBuilder<T> exportAsJson() {
    this.exportAsJson = true;
    return this;
  }

  public HttpExporter<T> build() {

    RetryPolicyCopy retryPolicyCopy = null;
    if (retryPolicy != null) {
      retryPolicyCopy =
          new RetryPolicyCopy(
              retryPolicy.getMaxAttempts(),
              retryPolicy.getInitialBackoff(),
              retryPolicy.getMaxBackoff(),
              retryPolicy.getBackoffMultiplier());
    }

    Map<String, String> headers = this.headers == null ? Collections.emptyMap() : this.headers;
    Supplier<Map<String, String>> headerSupplier = () -> headers;
    if (authenticator != null) {
      Authenticator auth = authenticator;
      headerSupplier =
          () -> {
            Map<String, String> headersCopy = new HashMap<>(headers);
            headersCopy.putAll(auth.getHeaders());
            return headersCopy;
          };
    }

    HttpSender sender =
        HttpSender.create(
            endpoint,
            compressionEnabled,
            timeoutNanos,
            headerSupplier,
            retryPolicyCopy,
            tlsConfigHelper.getSslSocketFactory(),
            tlsConfigHelper.getTrustManager(),
            tlsConfigHelper.getKeyManager());

    return new HttpExporter<>(exporterName, type, sender, meterProviderSupplier, exportAsJson);
  }
}
