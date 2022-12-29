/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.trace;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.traces.ResourceSpansMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.trace.v1.ResourceSpans;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

class OtlpHttpSpanExporterTest extends AbstractHttpTelemetryExporterTest<SpanData, ResourceSpans> {

  protected OtlpHttpSpanExporterTest() {
    super("span", "/v1/traces", ResourceSpans.getDefaultInstance());
  }

  @Override
  protected TelemetryExporterBuilder<SpanData> exporterBuilder() {
    OtlpHttpSpanExporterBuilder builder = OtlpHttpSpanExporter.builder();
    return new TelemetryExporterBuilder<SpanData>() {
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
      public TelemetryExporterBuilder<SpanData> setRetryPolicy(RetryPolicy retryPolicy) {
        RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<SpanData> setChannel(io.grpc.ManagedChannel channel) {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public TelemetryExporter<SpanData> build() {
        return TelemetryExporter.wrap(builder.build());
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
