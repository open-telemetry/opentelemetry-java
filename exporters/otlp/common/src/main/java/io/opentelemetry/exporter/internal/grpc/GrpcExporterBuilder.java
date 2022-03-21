/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/**
 * A builder for {@link GrpcExporter}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface GrpcExporterBuilder<T extends Marshaler> {
  GrpcExporterBuilder<T> setChannel(ManagedChannel channel);

  GrpcExporterBuilder<T> setTimeout(long timeout, TimeUnit unit);

  GrpcExporterBuilder<T> setTimeout(Duration timeout);

  GrpcExporterBuilder<T> setEndpoint(String endpoint);

  GrpcExporterBuilder<T> setCompression(String compressionMethod);

  GrpcExporterBuilder<T> setTrustedCertificates(byte[] trustedCertificatesPem);

  GrpcExporterBuilder<T> setClientTls(byte[] privateKeyPem, byte[] certificatePem);

  GrpcExporterBuilder<T> addHeader(String key, String value);

  GrpcExporterBuilder<T> setRetryPolicy(RetryPolicy retryPolicy);

  GrpcExporterBuilder<T> setMeterProvider(MeterProvider meterProvider);

  GrpcExporter<T> build();
}
