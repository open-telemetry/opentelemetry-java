/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.exporter.sender.jdk.internal.JdkHttpSender;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

class OtlpHttpSpanExporterJdkSenderTest
    extends AbstractHttpTelemetryExporterTest<SpanData, ResourceSpans> {

  protected OtlpHttpSpanExporterJdkSenderTest() {
    super("span", "/v1/traces", ResourceSpans.getDefaultInstance());
  }

  @Override
  protected boolean hasAuthenticatorSupport() {
    return false;
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> exporterBuilder() {
    OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder();
    return new TelemetryExporterBuilder<>() {
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
      public TelemetryExporterBuilder<SpanData> setChannel(io.grpc.ManagedChannel channel) {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public TelemetryExporter<SpanData> build() {
        OtlpHttpSpanExporter exporter = builder.build();
        assertThat(exporter)
            .extracting("delegate")
            .extracting("httpSender")
            .isInstanceOf(JdkHttpSender.class);
        return TelemetryExporter.wrap(exporter);
      }
    };
  }

  @Override
  protected SpanData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeSpanData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<SpanData> telemetry) {
    return ResourceSpansMarshaler.create(telemetry);
  }
}
