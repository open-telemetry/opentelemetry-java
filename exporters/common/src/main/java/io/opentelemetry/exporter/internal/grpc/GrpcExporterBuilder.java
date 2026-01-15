/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.compressor.Compressor;
import io.opentelemetry.exporter.grpc.GrpcSender;
import io.opentelemetry.exporter.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.compression.CompressorUtil;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.internal.ComponentId;
import io.opentelemetry.sdk.internal.StandardComponentId;
import java.net.URI;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * A builder for {@link GrpcExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@SuppressWarnings("JavadocMethod")
public class GrpcExporterBuilder {

  public static final long DEFAULT_CONNECT_TIMEOUT_SECS = 10;

  private static final Logger LOGGER = Logger.getLogger(GrpcExporterBuilder.class.getName());

  private final StandardComponentId.ExporterType exporterType;
  private final String fullMethodName;

  private Duration timeout;
  private Duration connectTimeout = Duration.ofSeconds(DEFAULT_CONNECT_TIMEOUT_SECS);
  private URI endpoint;
  @Nullable private Compressor compressor;
  private final Map<String, String> constantHeaders = new HashMap<>();
  private Supplier<Map<String, String>> headerSupplier = Collections::emptyMap;
  private TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();
  @Nullable private RetryPolicy retryPolicy = RetryPolicy.getDefault();
  private Supplier<MeterProvider> meterProviderSupplier =
      () -> GlobalOpenTelemetry.getOrNoop().getMeterProvider();
  private InternalTelemetryVersion internalTelemetryVersion = InternalTelemetryVersion.LEGACY;

  private ComponentLoader componentLoader =
      ComponentLoader.forClassLoader(GrpcExporterBuilder.class.getClassLoader());
  @Nullable private ExecutorService executorService;

  // Use Object type since gRPC may not be on the classpath.
  @Nullable private Object grpcChannel;

  public GrpcExporterBuilder(
      StandardComponentId.ExporterType exporterType,
      Duration defaultTimeout,
      URI defaultEndpoint,
      String fullMethodName) {
    this.exporterType = exporterType;
    this.fullMethodName = fullMethodName;
    timeout = defaultTimeout;
    endpoint = defaultEndpoint;
  }

  public GrpcExporterBuilder setChannel(ManagedChannel channel) {
    this.grpcChannel = channel;
    return this;
  }

  public GrpcExporterBuilder setTimeout(Duration timeout) {
    this.timeout = timeout.toNanos() == 0 ? Duration.ofNanos(Long.MAX_VALUE) : timeout;
    return this;
  }

  public GrpcExporterBuilder setConnectTimeout(Duration timeout) {
    connectTimeout = timeout.toNanos() == 0 ? Duration.ofNanos(Long.MAX_VALUE) : timeout;
    return this;
  }

  public GrpcExporterBuilder setEndpoint(String endpoint) {
    this.endpoint = ExporterBuilderUtil.validateEndpoint(endpoint);
    return this;
  }

  public GrpcExporterBuilder setCompression(@Nullable Compressor compressor) {
    this.compressor = compressor;
    return this;
  }

  /**
   * Sets the method used to compress payloads. If unset, compression is disabled. Compression
   * method "gzip" and "none" are supported out of the box. Support for additional compression
   * methods is available by implementing {@link Compressor} SPI.
   */
  public GrpcExporterBuilder setCompression(String compressionMethod) {
    Compressor compressor =
        CompressorUtil.validateAndResolveCompressor(compressionMethod, componentLoader);
    return setCompression(compressor);
  }

  public GrpcExporterBuilder setTrustManagerFromCerts(byte[] trustedCertificatesPem) {
    tlsConfigHelper.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  public GrpcExporterBuilder setKeyManagerFromCerts(byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  public GrpcExporterBuilder setSslContext(SSLContext sslContext, X509TrustManager trustManager) {
    tlsConfigHelper.setSslContext(sslContext, trustManager);
    return this;
  }

  public GrpcExporterBuilder addConstantHeader(String key, String value) {
    constantHeaders.put(key, value);
    return this;
  }

  public GrpcExporterBuilder setHeadersSupplier(Supplier<Map<String, String>> headerSupplier) {
    this.headerSupplier = headerSupplier;
    return this;
  }

  public GrpcExporterBuilder setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public GrpcExporterBuilder setMeterProvider(Supplier<MeterProvider> meterProviderSupplier) {
    this.meterProviderSupplier = meterProviderSupplier;
    return this;
  }

  public GrpcExporterBuilder setInternalTelemetryVersion(
      InternalTelemetryVersion internalTelemetryVersion) {
    this.internalTelemetryVersion = internalTelemetryVersion;
    return this;
  }

  public GrpcExporterBuilder setComponentLoader(ComponentLoader componentLoader) {
    this.componentLoader = componentLoader;
    return this;
  }

  public GrpcExporterBuilder setExecutorService(ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  @SuppressWarnings("BuilderReturnThis")
  public GrpcExporterBuilder copy() {
    GrpcExporterBuilder copy =
        new GrpcExporterBuilder(exporterType, timeout, endpoint, fullMethodName);

    copy.timeout = timeout;
    copy.connectTimeout = connectTimeout;
    copy.endpoint = endpoint;
    copy.compressor = compressor;
    copy.constantHeaders.putAll(constantHeaders);
    copy.headerSupplier = headerSupplier;
    copy.tlsConfigHelper = tlsConfigHelper.copy();
    if (retryPolicy != null) {
      copy.retryPolicy = retryPolicy.toBuilder().build();
    }
    copy.meterProviderSupplier = meterProviderSupplier;
    copy.internalTelemetryVersion = internalTelemetryVersion;
    copy.grpcChannel = grpcChannel;
    copy.componentLoader = componentLoader;
    return copy;
  }

  public GrpcExporter build() {
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

    boolean isPlainHttp = "http".equals(endpoint.getScheme());
    GrpcSenderProvider grpcSenderProvider = resolveGrpcSenderProvider();
    GrpcSender grpcSender =
        grpcSenderProvider.createSender(
            ImmutableGrpcSenderConfig.create(
                endpoint,
                fullMethodName,
                compressor,
                timeout,
                connectTimeout,
                headerSupplier,
                retryPolicy,
                isPlainHttp ? null : tlsConfigHelper.getSslContext(),
                isPlainHttp ? null : tlsConfigHelper.getTrustManager(),
                executorService,
                grpcChannel));
    LOGGER.log(Level.FINE, "Using GrpcSender: " + grpcSender.getClass().getName());

    return new GrpcExporter(
        grpcSender,
        internalTelemetryVersion,
        ComponentId.generateLazy(exporterType),
        meterProviderSupplier,
        endpoint);
  }

  public String toString(boolean includePrefixAndSuffix) {
    StringJoiner joiner =
        includePrefixAndSuffix
            ? new StringJoiner(", ", "GrpcExporterBuilder{", "}")
            : new StringJoiner(", ");
    joiner.add("endpoint=" + endpoint.toString());
    joiner.add("fullMethodName=" + fullMethodName);
    joiner.add("timeoutNanos=" + timeout.toNanos());
    joiner.add("connectTimeoutNanos=" + connectTimeout.toNanos());
    joiner.add(
        "compressorEncoding="
            + Optional.ofNullable(compressor).map(Compressor::getEncoding).orElse(null));
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
    if (grpcChannel != null) {
      joiner.add("grpcChannel=" + grpcChannel);
    }
    joiner.add("componentLoader=" + componentLoader);
    if (executorService != null) {
      joiner.add("executorService=" + executorService);
    }
    joiner.add("exporterType=" + exporterType.toString());
    joiner.add("internalTelemetrySchemaVersion=" + internalTelemetryVersion);
    // Note: omit tlsConfigHelper because we can't log the configuration in any readable way
    // Note: omit meterProviderSupplier because we can't log the configuration in any readable way
    return joiner.toString();
  }

  @Override
  public String toString() {
    return toString(true);
  }

  /**
   * Resolve the {@link GrpcSenderProvider}.
   *
   * <p>If no {@link GrpcSenderProvider} is available, throw {@link IllegalStateException}.
   *
   * <p>If only one {@link GrpcSenderProvider} is available, use it.
   *
   * <p>If multiple are available and..
   *
   * <ul>
   *   <li>{@code io.opentelemetry.exporter.grpc.GrpcSenderProvider} is empty, use the first found.
   *   <li>{@code io.opentelemetry.exporter.grpc.GrpcSenderProvider} is set, use the matching
   *       provider. If none match, throw {@link IllegalStateException}.
   * </ul>
   */
  private GrpcSenderProvider resolveGrpcSenderProvider() {
    Map<String, GrpcSenderProvider> grpcSenderProviders = new HashMap<>();
    for (GrpcSenderProvider spi : componentLoader.load(GrpcSenderProvider.class)) {
      grpcSenderProviders.put(spi.getClass().getName(), spi);
    }

    // No provider on classpath, throw
    if (grpcSenderProviders.isEmpty()) {
      throw new IllegalStateException(
          "No GrpcSenderProvider found on classpath. Please add dependency on "
              + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-managed-channel");
    }

    // Exactly one provider on classpath, use it
    if (grpcSenderProviders.size() == 1) {
      return grpcSenderProviders.values().stream().findFirst().get();
    }

    // If we've reached here, there are multiple GrpcSenderProviders
    String configuredSender =
        ConfigUtil.getString("io.opentelemetry.exporter.grpc.GrpcSenderProvider", "");

    // Multiple providers but none configured, use first we find and log a warning
    if (configuredSender.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Multiple GrpcSenderProvider found. Please include only one, "
              + "or specify preference setting io.opentelemetry.exporter.grpc.GrpcSenderProvider "
              + "to the FQCN of the preferred provider.");
      return grpcSenderProviders.values().stream().findFirst().get();
    }

    // Multiple providers with configuration match, use configuration match
    if (grpcSenderProviders.containsKey(configuredSender)) {
      return grpcSenderProviders.get(configuredSender);
    }

    // Multiple providers, configured does not match, throw
    throw new IllegalStateException(
        "No GrpcSenderProvider matched configured io.opentelemetry.exporter.grpc.GrpcSenderProvider: "
            + configuredSender);
  }
}
