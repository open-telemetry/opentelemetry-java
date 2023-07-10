/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.grpc.Channel;
import io.grpc.ManagedChannel;
import io.opentelemetry.api.GlobalOpenTelemetry;
import io.opentelemetry.api.internal.ConfigUtil;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.ExporterBuilderUtil;
import io.opentelemetry.exporter.internal.TlsConfigHelper;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.TimeUnit;
import java.util.function.BiFunction;
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
public class GrpcExporterBuilder<T extends Marshaler> {

  private static final Logger LOGGER = Logger.getLogger(GrpcExporterBuilder.class.getName());

  private final String exporterName;
  private final String type;
  private final String grpcEndpointPath;
  private final Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>>
      grpcStubFactory;

  private long timeoutNanos;
  private URI endpoint;
  private boolean compressionEnabled = false;
  private final Map<String, String> headers = new HashMap<>();
  private final TlsConfigHelper tlsConfigHelper = new TlsConfigHelper();
  @Nullable private RetryPolicy retryPolicy;
  private Supplier<MeterProvider> meterProviderSupplier = GlobalOpenTelemetry::getMeterProvider;

  // Use Object type since gRPC may not be on the classpath.
  @Nullable private Object grpcChannel;

  public GrpcExporterBuilder(
      String exporterName,
      String type,
      long defaultTimeoutSecs,
      URI defaultEndpoint,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> grpcStubFactory,
      String grpcEndpointPath) {
    this.exporterName = exporterName;
    this.type = type;
    this.grpcEndpointPath = grpcEndpointPath;
    timeoutNanos = TimeUnit.SECONDS.toNanos(defaultTimeoutSecs);
    endpoint = defaultEndpoint;
    this.grpcStubFactory = grpcStubFactory;
  }

  public GrpcExporterBuilder<T> setChannel(ManagedChannel channel) {
    this.grpcChannel = channel;
    return this;
  }

  public GrpcExporterBuilder<T> setTimeout(long timeout, TimeUnit unit) {
    timeoutNanos = unit.toNanos(timeout);
    return this;
  }

  public GrpcExporterBuilder<T> setTimeout(Duration timeout) {
    return setTimeout(timeout.toNanos(), TimeUnit.NANOSECONDS);
  }

  public GrpcExporterBuilder<T> setEndpoint(String endpoint) {
    this.endpoint = ExporterBuilderUtil.validateEndpoint(endpoint);
    return this;
  }

  public GrpcExporterBuilder<T> setCompression(String compressionMethod) {
    this.compressionEnabled = compressionMethod.equals("gzip");
    return this;
  }

  public GrpcExporterBuilder<T> setTrustManagerFromCerts(byte[] trustedCertificatesPem) {
    tlsConfigHelper.setTrustManagerFromCerts(trustedCertificatesPem);
    return this;
  }

  public GrpcExporterBuilder<T> setKeyManagerFromCerts(
      byte[] privateKeyPem, byte[] certificatePem) {
    tlsConfigHelper.setKeyManagerFromCerts(privateKeyPem, certificatePem);
    return this;
  }

  public GrpcExporterBuilder<T> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    tlsConfigHelper.setSslContext(sslContext, trustManager);
    return this;
  }

  public GrpcExporterBuilder<T> addHeader(String key, String value) {
    headers.put(key, value);
    return this;
  }

  public GrpcExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public GrpcExporterBuilder<T> setMeterProvider(MeterProvider meterProvider) {
    this.meterProviderSupplier = () -> meterProvider;
    return this;
  }

  public GrpcExporter<T> build() {
    GrpcSenderProvider grpcSenderProvider = resolveGrpcSenderProvider();
    GrpcSender<T> grpcSender =
        grpcSenderProvider.createSender(
            endpoint,
            grpcEndpointPath,
            compressionEnabled,
            timeoutNanos,
            headers,
            grpcChannel,
            grpcStubFactory,
            retryPolicy,
            tlsConfigHelper.getSslContext(),
            tlsConfigHelper.getTrustManager());
    LOGGER.log(Level.FINE, "Using GrpcSender: " + grpcSender.getClass().getName());

    return new GrpcExporter<>(exporterName, type, grpcSender, meterProviderSupplier);
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
   *   <li>{@code io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider} is empty, use the
   *       first found.
   *   <li>{@code io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider} is set, use the
   *       matching provider. If none match, throw {@link IllegalStateException}.
   * </ul>
   */
  private static GrpcSenderProvider resolveGrpcSenderProvider() {
    Map<String, GrpcSenderProvider> grpcSenderProviders = new HashMap<>();
    for (GrpcSenderProvider spi :
        ServiceLoader.load(GrpcSenderProvider.class, GrpcExporterBuilder.class.getClassLoader())) {
      grpcSenderProviders.put(spi.getClass().getName(), spi);
    }

    // No provider on classpath, throw
    if (grpcSenderProviders.isEmpty()) {
      throw new IllegalStateException(
          "No GrpcSenderProvider found on classpath. Please add dependency on "
              + "opentelemetry-exporter-sender-okhttp or opentelemetry-exporter-sender-grpc-upstream");
    }

    // Exactly one provider on classpath, use it
    if (grpcSenderProviders.size() == 1) {
      return grpcSenderProviders.values().stream().findFirst().get();
    }

    // If we've reached here, there are multiple GrpcSenderProviders
    String configuredSender =
        ConfigUtil.getString("io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider", "");

    // Multiple providers but none configured, use first we find and log a warning
    if (configuredSender.isEmpty()) {
      LOGGER.log(
          Level.WARNING,
          "Multiple GrpcSenderProvider found. Please include only one, "
              + "or specify preference setting io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider "
              + "to the FQCN of the preferred provider.");
      return grpcSenderProviders.values().stream().findFirst().get();
    }

    // Multiple providers with configuration match, use configuration match
    if (grpcSenderProviders.containsKey(configuredSender)) {
      return grpcSenderProviders.get(configuredSender);
    }

    // Multiple providers, configured does not match, throw
    throw new IllegalStateException(
        "No GrpcSenderProvider matched configured io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider: "
            + configuredSender);
  }
}
