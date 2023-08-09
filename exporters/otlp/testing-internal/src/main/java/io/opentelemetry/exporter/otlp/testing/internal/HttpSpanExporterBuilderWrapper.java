/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.otlp.http.trace.OtlpHttpSpanExporterBuilder;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/** Wrapper of {@link OtlpHttpSpanExporterBuilder} for use in integration tests. */
public class HttpSpanExporterBuilderWrapper implements TelemetryExporterBuilder<SpanData> {
  private final OtlpHttpSpanExporterBuilder builder;

  public HttpSpanExporterBuilderWrapper(OtlpHttpSpanExporterBuilder builder) {
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
  public TelemetryExporterBuilder<SpanData> setAuthenticator(Authenticator authenticator) {
    Authenticator.setAuthenticatorOnDelegate(builder, authenticator);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setTrustedCertificates(byte[] certificates) {
    builder.setTrustedCertificates(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setSslContext(
      SSLContext sslContext, X509TrustManager trustManager) {
    builder.setSslContext(sslContext, trustManager);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    builder.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setRetryPolicy(RetryPolicy retryPolicy) {
    builder.setRetryPolicy(retryPolicy);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<SpanData> setChannel(Object channel) {
    throw new UnsupportedOperationException("Not implemented");
  }

  @Override
  public TelemetryExporter<SpanData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
