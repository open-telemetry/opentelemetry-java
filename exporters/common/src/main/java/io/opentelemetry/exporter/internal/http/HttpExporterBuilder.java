/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.ServerAttributesUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.InternalTelemetrySchemaVersion;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.internal.ComponentId;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
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
  public static final long DEFAULT_CONNECT_TIMEOUT_SECS = 10;

  private static final Logger LOGGER = Logger.getLogger(HttpExporterBuilder.class.getName());

  private ComponentId.StandardExporterType exporterType;

  private String endpoint;

  private long timeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_TIMEOUT_SECS);
  @Nullable private Compressor compressor;
  private long connectTimeoutNanos = TimeUnit.SECONDS.toNanos(DEFAULT_CONNECT_TIMEOUT_SECS);
  @Nullable private ProxyOptions proxyOptions;
  private boolean exportAsJson = false;
  private final Map<String, String> constantHeaders = new HashMap<>();
  private Supplier<Map<String, String>> headerSupplier = Collections::emptyMap;

  private TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();
  @Nullable private RetryPolicy retryPolicy = RetryPolicy.getDefault();
  private Supplier<MeterProvider> meterProviderSupplier = GlobalOpenTelemetry::getMeterProvider;
  private InternalTelemetrySchemaVersion internalTelemetrySchemaVersion =
      InternalTelemetrySchemaVersion.LEGACY;
  private ClassLoader serviceClassLoader = HttpExporterBuilder.class.getClassLoader();
  @Nullable private ExecutorService executorService;

  public HttpExporterBuilder(
      ComponentId.StandardExporterType exporterType, String defaultEndpoint) {
    this.exporterType = exporterType;

    endpoint = defaultEndpoint;
  }

  public HttpExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = timeout == 0 ? Long.MAX_VALUE : unit.toNanos(timeout);
    return this;
  }

  public HttpExporterBuilder<T> setConnectTimeout(long timeout, TimeUnit unit) {
    connectTimeoutNanos = timeout == 0 ? Long.MAX_VALUE : unit.toNanos(timeout);
    return this;
  }

  public HttpExporterBuilder<T> setEndpoint(String endpoint) {
    URI uri = ExporterBuilderUtil.validateEndpoint(endpoint);
    this.endpoint = uri.toString();
    return this;
  }

  public HttpExporterBuilder<T> setCompression(@Nullable Compressor compressor) {
    this.compressor = compressor;
    return this;
  }

  public HttpExporterBuilder<T> addConstantHeaders(String key, String value) {
    constantHeaders.put(key, value);
    return this;
  }

  public HttpExporterBuilder<T> setHeadersSupplier(Supplier<Map<String, String>> headerSupplier) {
    this.headerSupplier = headerSupplier;
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

  public HttpExporterBuilder<T> setMeterProvider(Supplier<MeterProvider> meterProviderSupplier) {
    this.meterProviderSupplier = meterProviderSupplier;
    return this;
  }

  public HttpExporterBuilder<T> setInternalTelemetry(
      InternalTelemetrySchemaVersion internalTelemetrySchemaVersion) {
    this.internalTelemetrySchemaVersion = internalTelemetrySchemaVersion;
    return this;
  }

  public HttpExporterBuilder<T> setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public HttpExporterBuilder<T> setProxyOptions(ProxyOptions proxyOptions) {
    this.proxyOptions = proxyOptions;
    return this;
  }

  public HttpExporterBuilder<T> setServiceClassLoader(ClassLoader servieClassLoader) {
    this.serviceClassLoader = servieClassLoader;
    return this;
  }

  public HttpExporterBuilder<T> setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  public HttpExporterBuilder<T> exportAsJson() {
    this.exportAsJson = true;
    exporterType = mapToJsonTypeIfPossible(exporterType);
    return this;
  }

  private static ComponentId.StandardExporterType mapToJsonTypeIfPossible(
      ComponentId.StandardExporterType componentType) {
    switch (componentType) {
      case OTLP_HTTP_SPAN_EXPORTER:
        return ComponentId.StandardExporterType.OTLP_HTTP_JSON_SPAN_EXPORTER;
      case OTLP_HTTP_LOG_EXPORTER:
        return ComponentId.StandardExporterType.OTLP_HTTP_JSON_LOG_EXPORTER;
      case OTLP_HTTP_METRIC_EXPORTER:
        return ComponentId.StandardExporterType.OTLP_HTTP_JSON_METRIC_EXPORTER;
      default:
        return componentType;
    }
  }

  @SuppressWarnings("BuilderReturnThis")
  public HttpExporterBuilder<T> copy() {
    HttpExporterBuilder<T> copy = new HttpExporterBuilder<>(exporterType, endpoint);
    copy.endpoint = endpoint;
    copy.timeoutNanos = timeoutNanos;
    copy.connectTimeoutNanos = connectTimeoutNanos;
    copy.exportAsJson = exportAsJson;
    copy.compressor = compressor;
    copy.constantHeaders.putAll(constantHeaders);
    copy.headerSupplier = headerSupplier;
    copy.tlsConfigHelper = tlsConfigHelper.copy();
    if (retryPolicy != null) {
      copy.retryPolicy = retryPolicy.toBuilder().build();
    }
    copy.meterProviderSupplier = meterProviderSupplier;
    copy.internalTelemetrySchemaVersion = internalTelemetrySchemaVersion;
    copy.proxyOptions = proxyOptions;
    return copy;
  }

  public HttpExporter<T> build() {
    Supplier<Map<String, List<String>>> headerSupplier =
        () -> {
          Map<String, List<String>> result = new HashMap<>();
          Map<String, String> supplierResult = this.headerSupplier.get();
          if (supplierResult != null) {
            supplierResult.forEach(
                (key, value) -> result.put(key, Collections.singletonList(value)));
          }
          constantHeaders.forEach(
              (key, value) ->
                  result.merge(
                      key,
                      Collections.singletonList(value),
                      (v1, v2) -> {
                        List<String> merged = new ArrayList<>(v1);
                        merged.addAll(v2);
                        return merged;
                      }));
          return result;
        };

    boolean isPlainHttp = endpoint.startsWith("http://");
    HttpSenderProvider httpSenderProvider = resolveHttpSenderProvider();
    HttpSender httpSender =
        httpSenderProvider.createSender(
            HttpSenderConfig.create(
                endpoint,
                compressor,
                exportAsJson,
                exportAsJson ? "application/json" : "application/x-protobuf",
                timeoutNanos,
                connectTimeoutNanos,
                headerSupplier,
                proxyOptions,
                retryPolicy,
                isPlainHttp ? null : tlsConfigHelper.getSslContext(),
                isPlainHttp ? null : tlsConfigHelper.getTrustManager(),
                executorService));
    LOGGER.log(Level.FINE, "Using HttpSender: " + httpSender.getClass().getName());

    return new HttpExporter<>(
        ComponentId.generateLazy(exporterType),
        httpSender,
        meterProviderSupplier,
        internalTelemetrySchemaVersion,
        exporterType,
        ServerAttributesUtil.extractServerAttributes(endpoint));
  }

  public String toString(boolean includePrefixAndSuffix) {
    StringJoiner joiner =
        includePrefixAndSuffix
            ? new StringJoiner(", ", "HttpExporterBuilder{", "}")
            : new StringJoiner(", ");
    joiner.add("endpoint=" + endpoint);
    joiner.add("timeoutNanos=" + timeoutNanos);
    joiner.add("proxyOptions=" + proxyOptions);
    joiner.add(
        "compressorEncoding="
            + Optional.ofNullable(compressor).map(Compressor::getEncoding).orElse(null));
    joiner.add("connectTimeoutNanos=" + connectTimeoutNanos);
    joiner.add("exportAsJson=" + exportAsJson);
    StringJoiner headersJoiner = new StringJoiner(", ", "Headers{", "}");
    constantHeaders.forEach((key, value) -> headersJoiner.add(key + "=OBFUSCATED"));
    Map<String, String> headers = headerSupplier.get();
    if (headers != null) {
      headers.forEach((key, value) -> headersJoiner.add(key + "=OBFUSCATED"));
    }
    joiner.add("headers=" + headersJoiner);
    if (retryPolicy != null) {
      joiner.add("retryPolicy=" + retryPolicy);
    }
    joiner.add("serviceClassLoader=" + serviceClassLoader);
    if (executorService != null) {
      joiner.add("executorService=" + executorService);
    }
    joiner.add("exporterType=" + exporterType);
    joiner.add("internalTelemetrySchemaVersion=" + internalTelemetrySchemaVersion);
    // Note: omit tlsConfigHelper because we can't log the configuration in any readable way
    // Note: omit meterProviderSupplier because we can't log the configuration in any readable way
    return joiner.toString();
  }

  @Override
  public String toString() {
    return toString(true);
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
  private HttpSenderProvider resolveHttpSenderProvider() {
    Map<String, HttpSenderProvider> httpSenderProviders = new HashMap<>();
    for (HttpSenderProvider spi :
        ServiceLoader.load(HttpSenderProvider.class, serviceClassLoader)) {
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
}
