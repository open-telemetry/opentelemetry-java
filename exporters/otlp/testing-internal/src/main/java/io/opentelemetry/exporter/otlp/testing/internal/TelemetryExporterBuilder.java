/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.exporter.otlp.logs.OtlpGrpcLogRecordExporterBuilder;
import io.opentelemetry.exporter.otlp.metrics.OtlpGrpcMetricExporterBuilder;
import io.opentelemetry.exporter.otlp.profiles.OtlpGrpcProfilesExporterBuilder;
import io.opentelemetry.exporter.otlp.profiles.ProfileData;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporterBuilder;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import io.opentelemetry.sdk.logs.data.LogRecordData;
import io.opentelemetry.sdk.metrics.data.MetricData;
import io.opentelemetry.sdk.trace.data.SpanData;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

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

  static TelemetryExporterBuilder<ProfileData> wrap(OtlpGrpcProfilesExporterBuilder builder) {
    return new GrpcProfilesExporterBuilderWrapper(builder);
  }

  TelemetryExporterBuilder<T> setEndpoint(String endpoint);

  TelemetryExporterBuilder<T> setTimeout(long timeout, TimeUnit unit);

  TelemetryExporterBuilder<T> setTimeout(Duration timeout);

  TelemetryExporterBuilder<T> setConnectTimeout(long timeout, TimeUnit unit);

  TelemetryExporterBuilder<T> setConnectTimeout(Duration timeout);

  TelemetryExporterBuilder<T> setCompression(String compression);

  TelemetryExporterBuilder<T> addHeader(String key, String value);

  TelemetryExporterBuilder<T> setHeaders(Supplier<Map<String, String>> headerSupplier);

  TelemetryExporterBuilder<T> setTrustedCertificates(byte[] certificates);

  TelemetryExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem);

  TelemetryExporterBuilder<T> setSslContext(SSLContext sslContext, X509TrustManager trustManager);

  TelemetryExporterBuilder<T> setRetryPolicy(@Nullable RetryPolicy retryPolicy);

  TelemetryExporterBuilder<T> setProxyOptions(ProxyOptions proxyOptions);

  TelemetryExporterBuilder<T> setChannel(Object channel);

  TelemetryExporterBuilder<T> setServiceClassLoader(ClassLoader serviceClassLoader);

  TelemetryExporterBuilder<T> setComponentLoader(ComponentLoader componentLoader);

  TelemetryExporterBuilder<T> setExecutorService(ExecutorService executorService);

  TelemetryExporterBuilder<T> setMeterProvider(Supplier<MeterProvider> meterProviderSupplier);

  TelemetryExporterBuilder<T> setInternalTelemetryVersion(InternalTelemetryVersion schemaVersion);

  TelemetryExporter<T> build();
}
