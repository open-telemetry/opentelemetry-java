/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface TelemetryExporterBuilder<T> {

  static TelemetryExporterBuilder<SpanData> wrap(OtlpGrpcSpanExporterBuilder builder) {
    return new GrpcSpanExporterBuilderWrapper(builder);
  }

  static TelemetryExporterBuilder<MetricData> wrap(OtlpGrpcMetricExporterBuilder builder) {
    return new GrpcMetricExporterBuilderWrapper(builder);
  }

  static TelemetryExporterBuilder<LogRecordData> wrap(OtlpGrpcLogRecordExporterBuilder builder) {
    return new GrpcLogRecordExporterBuilderWrapper(builder);
  }

  TelemetryExporterBuilder<T> setEndpoint(String endpoint);

  TelemetryExporterBuilder<T> setTimeout(long timeout, TimeUnit unit);

  TelemetryExporterBuilder<T> setTimeout(Duration timeout);

  TelemetryExporterBuilder<T> setCompression(String compression);

  TelemetryExporterBuilder<T> addHeader(String key, String value);

  TelemetryExporterBuilder<T> setTrustedCertificates(byte[] certificates);

  TelemetryExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem);

  TelemetryExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy);

  TelemetryExporterBuilder<T> setChannel(ManagedChannel channel);

  TelemetryExporter<T> build();
}
