/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.http.logs;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.otlp.logs.ResourceLogsMarshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.AbstractHttpTelemetryExporterTest;
import io.opentelemetry.exporter.otlp.testing.internal.FakeTelemetryUtil;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporter;
import io.opentelemetry.exporter.otlp.testing.internal.TelemetryExporterBuilder;
import io.opentelemetry.proto.logs.v1.ResourceLogs;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

class OtlpHttpLogRecordExporterTest
    extends AbstractHttpTelemetryExporterTest<LogRecordData, ResourceLogs> {

  protected OtlpHttpLogRecordExporterTest() {
    super("log", "/v1/logs", ResourceLogs.getDefaultInstance());
  }

  @Override
  protected TelemetryExporterBuilder<LogRecordData> exporterBuilder() {
    OtlpHttpLogRecordExporterBuilder builder = OtlpHttpLogRecordExporter.builder();
    return new TelemetryExporterBuilder<LogRecordData>() {
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
      public TelemetryExporterBuilder<LogRecordData> setRetryPolicy(RetryPolicy retryPolicy) {
        RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy);
        return this;
      }

      @Override
      public TelemetryExporterBuilder<LogRecordData> setChannel(io.grpc.ManagedChannel channel) {
        throw new UnsupportedOperationException("Not implemented");
      }

      @Override
      public TelemetryExporter<LogRecordData> build() {
        return TelemetryExporter.wrap(builder.build());
      }
    };
  }

  @Override
  protected LogRecordData generateFakeTelemetry() {
    return FakeTelemetryUtil.generateFakeLogRecordData();
  }

  @Override
  protected Marshaler[] toMarshalers(List<LogRecordData> telemetry) {
    return ResourceLogsMarshaler.create(telemetry);
  }
}
