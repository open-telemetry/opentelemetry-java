/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
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

  private static final Logger LOGGER = Logger.getLogger(HttpExporterBuilder.class.getName());

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

  public HttpExporterBuilder<T> setTrustManagerFromCerts(byte[] trustedCertificatesPem) {
    tlsConfigHelper.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  public HttpExporterBuilder<T> setKeyManagerFromCerts(
      byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  public HttpExporterBuilder<T> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    tlsConfigHelper.setSslContext(sslContext, trustManager);
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

  /**
   * Resolve the {@link HttpSenderProvider}.
   *
   * <p>If no {@link HttpSenderProvider} is available, throw {@link IllegalStateException}.
   *
   * <p>If only one {@link HttpSenderProvider} is available, use it.
   *
   * <p>If multiple are available and..
   *
   * <ul>
   *   <li>{@code io.opentelemetry.exporter.internal.http.HttpSenderProvider} is empty, use the
   *       first found.
   *   <li>{@code io.opentelemetry.exporter.internal.http.HttpSenderProvider} is set, use the
   *       matching provider. If none match, throw {@link IllegalStateException}.
   * </ul>
   */
  private static HttpSenderProvider resolveHttpSenderProvider() {
    Map<String, HttpSenderProvider> httpSenderProviders = new HashMap<>();
    for (HttpSenderProvider spi :
        ServiceLoader.load(HttpSenderProvider.class, HttpExporterBuilder.class.getClassLoader())) {
      httpSenderProviders.put(spi.getClass().getName(), spi);
    }

    // No provider on classpath, throw
    if (httpSenderProviders.isEmpty()) {
      throw new IllegalStateException(
          "No HttpSenderProvider found on classpath. Please add dependency on "
              + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-jdk");
    }

    // Exactly one provider on classpath, use it
    if (httpSenderProviders.size() == 1) {
      return httpSenderProviders.values().stream().findFirst().get();
    }

    // If we've reached here, there are multiple HttpSenderProviders
    String configuredSender =
        ConfigUtil.getString("io.opentelemetry.exporter.internal.http.HttpSenderProvider", "");

    // Multiple providers but none configured, use first we find and log a warning
    if (configuredSender.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Multiple HttpSenderProvider found. Please include only one, "
              + "or specify preference setting io.opentelemetry.exporter.internal.http.HttpSenderProvider "
              + "to the FQCN of the preferred provider.");
      return httpSenderProviders.values().stream().findFirst().get();
    }

    // Multiple providers with configuration match, use configuration match
    if (httpSenderProviders.containsKey(configuredSender)) {
      return httpSenderProviders.get(configuredSender);
    }

    // Multiple providers, configured does not match, throw
    throw new IllegalStateException(
        "No HttpSenderProvider matched configured io.opentelemetry.exporter.internal.http.HttpSenderProvider: "
            + configuredSender);
  }

  public HttpExporter<T> build() {
    Map<String, String> headers = this.headers == null ? Collections.emptyMap() : this.headers;
    Supplier<Map<String, String>> headerSupplier = () -> headers;

    HttpSenderProvider httpSenderProvider = resolveHttpSenderProvider();
    HttpSender httpSender =
        httpSenderProvider.createSender(
            endpoint,
            compressionEnabled,
            exportAsJson ? "application/json" : "application/x-protobuf",
            timeoutNanos,
            headerSupplier,
            authenticator,
            retryPolicy,
            tlsConfigHelper.getSslContext(),
            tlsConfigHelper.getTrustManager());
    LOGGER.log(Level.FINE, "Using HttpSender: " + httpSender.getClass().getName());

    return new HttpExporter<>(exporterName, type, httpSender, meterProviderSupplier, exportAsJson);
  }
}
