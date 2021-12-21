/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.grpc;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

public interface GrpcServiceBuilder<REQ extends Marshaler, RES extends Marshaler> {
  GrpcServiceBuilder<REQ, RES> setChannel(ManagedChannel channel);

  GrpcServiceBuilder<REQ, RES> setTimeout(long timeout, TimeUnit unit);

  GrpcServiceBuilder<REQ, RES> setTimeout(Duration timeout);

  GrpcServiceBuilder<REQ, RES> setEndpoint(String endpoint);

  GrpcServiceBuilder<REQ, RES> setCompression(String compressionMethod);

  GrpcServiceBuilder<REQ, RES> setTrustedCertificates(byte[] trustedCertificatesPem);

  GrpcServiceBuilder<REQ, RES> addHeader(String key, String value);

  GrpcServiceBuilder<REQ, RES> addRetryPolicy(RetryPolicy retryPolicy);

  GrpcServiceBuilder<REQ, RES> setMeterProvider(MeterProvider meterProvider);

  GrpcService<REQ, RES> build();
}
