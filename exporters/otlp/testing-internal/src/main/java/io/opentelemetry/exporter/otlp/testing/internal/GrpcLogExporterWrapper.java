/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.internal.retry.RetryUtil;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogExporterBuilder;
import io.opentelemetry.sdk.logs.data.LogData;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

final class GrpcLogExporterWrapper implements TelemetryExporterBuilder<LogData> {
  private final OtlpGrpcLogExporterBuilder builder;

  GrpcLogExporterWrapper(OtlpGrpcLogExporterBuilder builder) {
    this.builder = builder;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setEndpoint(String endpoint) {
    builder.setEndpoint(endpoint);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setTimeout(long timeout, TimeUnit unit) {
    builder.setTimeout(timeout, unit);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setTimeout(Duration timeout) {
    builder.setTimeout(timeout);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setCompression(String compression) {
    builder.setCompression(compression);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> addHeader(String key, String value) {
    builder.addHeader(key, value);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setTrustedCertificates(byte[] certificates) {
    builder.setTrustedCertificates(certificates);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setClientTls(
      byte[] privateKeyPem, byte[] certificatePem) {
    builder.setClientTls(privateKeyPem, certificatePem);
    return this;
  }

  @Override
  public TelemetryExporterBuilder<LogData> setRetryPolicy(RetryPolicy retryPolicy) {
    RetryUtil.setRetryPolicyOnDelegate(builder, retryPolicy);
    return this;
  }

  @Override
  @SuppressWarnings("deprecation") // testing deprecated functionality
  public TelemetryExporterBuilder<LogData> setChannel(ManagedChannel channel) {
    builder.setChannel(channel);
    return this;
  }

  @Override
  public TelemetryExporter<LogData> build() {
    return TelemetryExporter.wrap(builder.build());
  }
}
