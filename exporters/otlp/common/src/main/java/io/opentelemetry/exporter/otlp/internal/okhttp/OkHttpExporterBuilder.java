/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.okhttp;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.TlsUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.net.ssl.SSLException;
import javax.net.ssl.X509TrustManager;
import okhttp3.Headers;
import okhttp3.OkHttpClient;

/** A builder for {@link OkHttpExporter}. */
@SuppressWarnings("checkstyle:JavadocMethod")
public final class OkHttpExporterBuilder<T extends Marshaler> {
  public static final long DEFAULT_TIMEOUT_SECS = 10;

  private final String type;

  private String endpoint;

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  private boolean compressionEnabled = false;
  @Nullable private Headers.Builder headersBuilder;
  @Nullable private byte[] trustedCertificatesPem;
  private MeterProvider meterProvider = MeterProvider.noop();

  public OkHttpExporterBuilder(String type, String defaultEndpoint) {
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

    this.endpoint = endpoint;
    return this;
  }

  public OkHttpExporterBuilder<T> setCompression(String compressionMethod) {
    if (compressionMethod.equals("gzip")) {
      this.compressionEnabled = true;
    }
    return this;
  }

  public OkHttpExporterBuilder<T> addHeader(String key, String value) {
    if (headersBuilder == null) {
      headersBuilder = new Headers.Builder();
    }
    headersBuilder.add(key, value);
    return this;
  }

  public OkHttpExporterBuilder<T> setTrustedCertificates(byte[] trustedCertificatesPem) {
    this.trustedCertificatesPem = trustedCertificatesPem;
    return this;
  }

  public OkHttpExporterBuilder<T> setMeterProvider(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
    return this;
  }

  public OkHttpExporter<T> build() {
    OkHttpClient.Builder clientBuilder =
        new OkHttpClient.Builder()
            .dispatcher(OkHttpUtil.newDispatcher())
            .callTimeout(Duration.ofNanos(timeoutNanos));

    if (trustedCertificatesPem != null) {
      try {
        X509TrustManager trustManager = TlsUtil.trustManager(trustedCertificatesPem);
        clientBuilder.sslSocketFactory(TlsUtil.sslSocketFactory(trustManager), trustManager);
      } catch (SSLException e) {
        throw new IllegalStateException(
            "Could not set trusted certificate for OTLP HTTP connection, are they valid X.509 in PEM format?",
            e);
      }
    }

    Headers headers = headersBuilder == null ? null : headersBuilder.build();

    return new OkHttpExporter<>(
        type, clientBuilder.build(), meterProvider, endpoint, headers, compressionEnabled);
  }
}
