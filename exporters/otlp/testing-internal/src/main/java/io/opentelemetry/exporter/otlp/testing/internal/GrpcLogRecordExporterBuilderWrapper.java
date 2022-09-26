/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
  @SuppressWarnings("deprecation") // testing deprecated functionality
  public TelemetryExporterBuilder<LogRecordData> setChannel(ManagedChannel channel) {
    builder.setChannel(channel);
    return this;
  }

  @Override
  public TelemetryExporter<LogRecordData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
