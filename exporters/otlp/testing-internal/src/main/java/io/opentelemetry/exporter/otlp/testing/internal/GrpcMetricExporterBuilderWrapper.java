/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.metrics.data.MetricData;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

final class GrpcMetricExporterBuilderWrapper implements TelemetryExporterBuilder<MetricData> {
  private final OtlpGrpcMetricExporterBuilder builder;

  GrpcMetricExporterBuilderWrapper(OtlpGrpcMetricExporterBuilder builder) {
    this.builder = builder;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setEndpoint(String endpoint) {
    builder.setEndpoint(endpoint);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setTimeout(long timeout, TimeUnit unit) {
    builder.setTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setTimeout(Duration timeout) {
    builder.setTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setConnectTimeout(long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setConnectTimeout(Duration timeout) {
    builder.setConnectTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setCompression(String compression) {
    builder.setCompression(compression);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> addHeader(String key, String value) {
    builder.addHeader(key, value);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setHeaders(
      Supplier<Map<String, String>> headerSupplier) {
    builder.setHeaders(headerSupplier);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setAuthenticator(Authenticator authenticator) {
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setTrustedCertificates(byte[] certificates) {
    builder.setTrustedCertificates(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    builder.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    builder.setSslContext(sslContext, trustManager);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<MetricData> setRetryPolicy(RetryPolicy retryPolicy) {
    builder.setRetryPolicy(retryPolicy);
    return this;
  }

  @Override
  @SuppressWarnings("deprecation") // testing deprecated functionality
  public TelemetryExporterBuilder<MetricData> setChannel(Object channel) {
    builder.setChannel((ManagedChannel) channel);
    return this;
  }

  @Override
  public TelemetryExporter<MetricData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
