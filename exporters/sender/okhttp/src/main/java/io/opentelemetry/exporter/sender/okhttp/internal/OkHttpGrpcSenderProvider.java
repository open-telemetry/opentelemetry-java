/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderConfig;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.marshal.Marshaler;

/**
 * {@link GrpcSender} SPI implementation for {@link OkHttpGrpcSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OkHttpGrpcSenderProvider implements GrpcSenderProvider {

  @Override
  public <T extends Marshaler> GrpcSender<T> createSender(GrpcSenderConfig<T> grpcSenderConfig) {
    return new OkHttpGrpcSenderBuilder<T>(
            grpcSenderConfig.getEndpoint().resolve(grpcSenderConfig.getEndpointPath()).toString(),
            grpcSenderConfig.getTimeoutNanos(),
            grpcSenderConfig.getConnectTimeoutNanos(),
            grpcSenderConfig.getHeadersSupplier())
        .setCompressor(grpcSenderConfig.getCompressor())
        .setRetryPolicy(grpcSenderConfig.getRetryPolicy())
        .setSslContext(grpcSenderConfig.getSslContext())
        .setTrustManager(grpcSenderConfig.getTrustManager())
        .setExecutorService(grpcSenderConfig.getExecutorService())
        .createOkHttpGrpcSender();
  }
}
