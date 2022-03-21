/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.testing.internal;

import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface TelemetryExporterBuilder<T> {
  TelemetryExporterBuilder<T> setEndpoint(String endpoint);

  TelemetryExporterBuilder<T> setTimeout(long timeout, TimeUnit unit);

  TelemetryExporterBuilder<T> setTimeout(Duration timeout);

  TelemetryExporterBuilder<T> setCompression(String compression);

  TelemetryExporterBuilder<T> addHeader(String key, String value);

  TelemetryExporterBuilder<T> setTrustedCertificates(byte[] certificates);

  TelemetryExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem);

  TelemetryExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy);

  TelemetryExporter<T> build();
}
