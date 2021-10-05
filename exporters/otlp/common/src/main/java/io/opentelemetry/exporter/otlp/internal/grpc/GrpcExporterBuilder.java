/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

/** A builder for {@link GrpcExporter}. */
public interface GrpcExporterBuilder<T extends Marshaler> {
  GrpcExporterBuilder<T> setChannel(ManagedChannel channel);

  GrpcExporterBuilder<T> setTimeout(long timeout, TimeUnit unit);

  GrpcExporterBuilder<T> setTimeout(Duration timeout);

  GrpcExporterBuilder<T> setEndpoint(String endpoint);

  GrpcExporterBuilder<T> setCompression(String compressionMethod);

  GrpcExporterBuilder<T> setTrustedCertificates(byte[] trustedCertificatesPem);

  GrpcExporterBuilder<T> addHeader(String key, String value);

  GrpcExporter<T> build();
}
