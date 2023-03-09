/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.okhttp;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.retry.RetryInterceptor;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.X509KeyManager;
import javax.net.ssl.X509TrustManager;
import okhttp3.Headers;
import okhttp3.OkHttpClient;
import okhttp3.Request;

/**
 * A builder for {@link OkHttpExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class OkHttpExporterBuilder<T extends Marshaler> {
  public static final long DEFAULT_TIMEOUT_SECS = 10;

  private final String exporterName;
  private final String type;

  private String endpoint;

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private boolean compressionEnabled = false;
  private boolean exportAsJson = false;
  @Nullable private Headers.Builder headersBuilder;

  private final TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();
  @Nullable private RetryPolicy retryPolicy;
  private Supplier<MeterProvider> meterProviderSupplier = GlobalOpenTelemetry::getMeterProvider;
  @Nullable private Authenticator authenticator;

  public OkHttpExporterBuilder(String exporterName, String type, String defaultEndpoint) {
    this.exporterName = exporterName;
    this.type = type;

    endpoint = defaultEndpoint;
  }

  public OkHttpExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  public OkHttpExporterBuilder<T> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  public OkHttpExporterBuilder<T> setEndpoint(String endpoint) {
    URI uri = ExporterBuilderUtil.validateEndpoint(endpoint);
    this.endpoint = uri.toString();
    return this;
  }

  public OkHttpExporterBuilder<T> setCompression(String compressionMethod) {
    this.compressionEnabled = compressionMethod.equals("gzip");
    return this;
  }

  public OkHttpExporterBuilder<T> addHeader(String key, String value) {
    if (headersBuilder == null) {
      headersBuilder = new Headers.Builder();
    }
    headersBuilder.add(key, value);
    return this;
  }

  public OkHttpExporterBuilder<T> setAuthenticator(Authenticator authenticator) {
    this.authenticator = authenticator;
    return this;
  }

  public OkHttpExporterBuilder<T> configureTrustManager(byte[] trustedCertificatesPem) {
    tlsConfigHelper.createTrustManager(trustedCertificatesPem);
    return this;
  }

  public OkHttpExporterBuilder<T> setTrustManager(X509TrustManager trustManager) {
    tlsConfigHelper.setTrustManager(trustManager);
    return this;
  }

  public OkHttpExporterBuilder<T> configureKeyManager(byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.createKeyManager(privateKeyPem, certificatePem);
    return this;
  }

  public OkHttpExporterBuilder<T> setKeyManager(X509KeyManager keyManager) {
    tlsConfigHelper.setKeyManager(keyManager);
    return this;
  }

  public OkHttpExporterBuilder<T> setSslSocketFactory(SSLSocketFactory sslSocketFactory) {
    tlsConfigHelper.setSslSocketFactory(sslSocketFactory);
    return this;
  }

  public OkHttpExporterBuilder<T> setMeterProvider(MeterProvider meterProvider) {
    this.meterProviderSupplier = () -> meterProvider;
    return this;
  }

  public OkHttpExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public OkHttpExporterBuilder<T> exportAsJson() {
    this.exportAsJson = true;
    return this;
  }

  public OkHttpExporter<T> build() {
    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder()
            .dispatcher(OkHttpUtil.newDispatcher())
            .callTimeout(Duration.ofNanos(timeoutNanos));

    tlsConfigHelper.configureWithSocketFactory(clientBuilder::sslSocketFactory);

    Headers headers = headersBuilder == null ? null : headersBuilder.build();

    if (retryPolicy != null) {
      clientBuilder.addInterceptor(new RetryInterceptor(retryPolicy, OkHttpExporter::isRetryable));
    }

    if (authenticator != null) {
      Authenticator finalAuthenticator = authenticator;
      // Generate and attach OkHttp Authenticator implementation
      clientBuilder.authenticator(
          (route, response) -> {
            Request.Builder requestBuilder = response.request().newBuilder();
            finalAuthenticator.getHeaders().forEach(requestBuilder::header);
            return requestBuilder.build();
          });
    }

    return new OkHttpExporter<>(
        exporterName,
        type,
        clientBuilder.build(),
        meterProviderSupplier,
        endpoint,
        headers,
        compressionEnabled,
        exportAsJson);
  }
}
