/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

final class GrpcLogRecordExporterBuilderWrapper implements TelemetryExporterBuilder<LogRecordData> {
  private final OtlpGrpcLogRecordExporterBuilder builder;

  GrpcLogRecordExporterBuilderWrapper(OtlpGrpcLogRecordExporterBuilder builder) {
    this.builder = builder;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setEndpoint(String endpoint) {
    builder.setEndpoint(endpoint);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setTimeout(long timeout, TimeUnit unit) {
    builder.setTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setTimeout(Duration timeout) {
    builder.setTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setConnectTimeout(long timeout, TimeUnit unit) {
    builder.setConnectTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setConnectTimeout(Duration timeout) {
    builder.setConnectTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setCompression(String compression) {
    builder.setCompression(compression);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> addHeader(String key, String value) {
    builder.addHeader(key, value);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setHeaders(
      Supplier<Map<String, String>> headerSupplier) {
    builder.setHeaders(headerSupplier);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setTrustedCertificates(byte[] certificates) {
    builder.setTrustedCertificates(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    builder.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    builder.setSslContext(sslContext, trustManager);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    builder.setRetryPolicy(retryPolicy);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setProxyOptions(ProxyOptions proxyOptions) {
    throw new UnsupportedOperationException("ProxyOptions are not supported for gRPC");
  }

  @Override
  @SuppressWarnings("deprecation") // testing deprecated functionality
  public TelemetryExporterBuilder<LogRecordData> setChannel(Object channel) {
    builder.setChannel((ManagedChannel) channel);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setServiceClassLoader(
      ClassLoader serviceClassLoader) {
    builder.setServiceClassLoader(serviceClassLoader);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setComponentLoader(
      ComponentLoader componentLoader) {
    builder.setComponentLoader(componentLoader);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setExecutorService(
      ExecutorService executorService) {
    builder.setExecutorService(executorService);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setMeterProvider(
      Supplier<MeterProvider> meterProviderSupplier) {
    builder.setMeterProvider(meterProviderSupplier);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogRecordData> setInternalTelemetryVersion(
      InternalTelemetryVersion schemaVersion) {
    builder.setInternalTelemetryVersion(schemaVersion);
    return this;
  }

  @Override
  public TelemetryExporter<LogRecordData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
