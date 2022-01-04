/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.exporter.otlp.internal.Marshaler;
import io.opentelemetry.exporter.otlp.internal.RetryPolicy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

interface GrpcServiceBuilder<ReqT extends Marshaler, ResT extends UnMarshaller> {
  GrpcServiceBuilder<ReqT, ResT> setChannel(ManagedChannel channel);

  GrpcServiceBuilder<ReqT, ResT> setTimeout(long timeout, TimeUnit unit);

  GrpcServiceBuilder<ReqT, ResT> setTimeout(Duration timeout);

  GrpcServiceBuilder<ReqT, ResT> setEndpoint(String endpoint);

  GrpcServiceBuilder<ReqT, ResT> setCompression(String compressionMethod);

  GrpcServiceBuilder<ReqT, ResT> setTrustedCertificates(byte[] trustedCertificatesPem);

  GrpcServiceBuilder<ReqT, ResT> addHeader(String key, String value);

  GrpcServiceBuilder<ReqT, ResT> addRetryPolicy(RetryPolicy retryPolicy);

  GrpcServiceBuilder<ReqT, ResT> setMeterProvider(MeterProvider meterProvider);

  GrpcService<ReqT, ResT> build();
}
