/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/** Wrapper of {@link OtlpGrpcSpanExporterBuilder} for use in integration tests. */
final class GrpcSpanExporterBuilderWrapper implements TelemetryExporterBuilder<SpanData> {
  private final OtlpGrpcSpanExporterBuilder builder;

  GrpcSpanExporterBuilderWrapper(OtlpGrpcSpanExporterBuilder builder) {
    this.builder = builder;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setEndpoint(String endpoint) {
    builder.setEndpoint(endpoint);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setTimeout(long timeout, TimeUnit unit) {
    builder.setTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setTimeout(Duration timeout) {
    builder.setTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setConnectTimeout(long timeout, TimeUnit unit) {
    throw new UnsupportedOperationException();
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setConnectTimeout(Duration timeout) {
    builder.setConnectTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setCompression(String compression) {
    builder.setCompression(compression);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> addHeader(String key, String value) {
    builder.addHeader(key, value);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setHeaders(
      Supplier<Map<String, String>> headerSupplier) {
    builder.setHeaders(headerSupplier);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setAuthenticator(Authenticator authenticator) {
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setTrustedCertificates(byte[] certificates) {
    builder.setTrustedCertificates(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    builder.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    builder.setSslContext(sslContext, trustManager);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setRetryPolicy(RetryPolicy retryPolicy) {
    builder.setRetryPolicy(retryPolicy);
    return this;
  }

  @Override
  @SuppressWarnings("deprecation") // testing deprecated functionality
  public TelemetryExporterBuilder<SpanData> setChannel(Object channel) {
    builder.setChannel((ManagedChannel) channel);
    return this;
  }

  @Override
  public TelemetryExporter<SpanData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
