/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.profiles.OtlpGrpcProfilesExporterBuilder;
import io.opentelemetry.exporter.otlp.profiles.ProfileData;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

final class GrpcProfilesExporterBuilderWrapper implements TelemetryExporterBuilder<ProfileData> {
  private final OtlpGrpcProfilesExporterBuilder builder;

  GrpcProfilesExporterBuilderWrapper(OtlpGrpcProfilesExporterBuilder builder) {
    this.builder = builder;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setEndpoint(String endpoint) {
    builder.setEndpoint(endpoint);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setTimeout(long timeout, TimeUnit unit) {
    builder.setTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setTimeout(Duration timeout) {
    builder.setTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setConnectTimeout(long timeout, TimeUnit unit) {
    builder.setConnectTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setConnectTimeout(Duration timeout) {
    builder.setConnectTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setCompression(String compression) {
    builder.setCompression(compression);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> addHeader(String key, String value) {
    builder.addHeader(key, value);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setHeaders(
      Supplier<Map<String, String>> headerSupplier) {
    builder.setHeaders(headerSupplier);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setTrustedCertificates(byte[] certificates) {
    builder.setTrustedCertificates(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    builder.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    builder.setSslContext(sslContext, trustManager);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    builder.setRetryPolicy(retryPolicy);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setProxyOptions(ProxyOptions proxyOptions) {
    throw new UnsupportedOperationException("ProxyOptions are not supported for gRPC");
  }

  @Override
  @SuppressWarnings("deprecation") // testing deprecated functionality
  public TelemetryExporterBuilder<ProfileData> setChannel(Object channel) {
    builder.setChannel((ManagedChannel) channel);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setServiceClassLoader(
      ClassLoader serviceClassLoader) {
    builder.setServiceClassLoader(serviceClassLoader);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setComponentLoader(ComponentLoader componentLoader) {
    builder.setComponentLoader(componentLoader);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setExecutorService(ExecutorService executorService) {
    builder.setExecutorService(executorService);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setMeterProvider(
      Supplier<MeterProvider> meterProviderSupplier) {
    // Not yet supported
    return this;
  }

  @Override
  public TelemetryExporterBuilder<ProfileData> setInternalTelemetryVersion(
      InternalTelemetryVersion schemaVersion) {
    // Not yet supported
    return this;
  }

  @Override
  public TelemetryExporter<ProfileData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
