/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.trace.jaeger.sampler;

import io.grpc.ManagedChannel;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.exporter.internal.retry.RetryPolicy;
import java.time.Duration;
import java.util.concurrent.TimeUnit;

interface GrpcServiceBuilder<ReqMarshalerT extends Marshaler, ResUnMarshalerT extends UnMarshaler> {
  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setChannel(ManagedChannel channel);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setTimeout(long timeout, TimeUnit unit);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setTimeout(Duration timeout);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setEndpoint(String endpoint);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setCompression(String compressionMethod);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setTrustedCertificates(
      byte[] trustedCertificatesPem);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> setClientTls(
      byte[] privateKeyPem, byte[] privateKeyChainPem);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> addHeader(String key, String value);

  GrpcServiceBuilder<ReqMarshalerT, ResUnMarshalerT> addRetryPolicy(RetryPolicy retryPolicy);

  GrpcService<ReqMarshalerT, ResUnMarshalerT> build();
}
