/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.grpc.GrpcSender;
import io.opentelemetry.exporter.grpc.GrpcSenderConfig;
import io.opentelemetry.exporter.grpc.GrpcSenderProvider;

/**
 * {@link GrpcSender} SPI implementation for {@link OkHttpGrpcSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OkHttpGrpcSenderProvider implements GrpcSenderProvider {

  @Override
  public GrpcSender createSender(GrpcSenderConfig grpcSenderConfig) {
    return new OkHttpGrpcSender(
        grpcSenderConfig
            .getEndpoint()
            .resolve("/" + grpcSenderConfig.getServiceAndMethodName())
            .toString(),
        grpcSenderConfig.getCompressor(),
        grpcSenderConfig.getTimeoutNanos(),
        grpcSenderConfig.getConnectTimeoutNanos(),
        grpcSenderConfig.getHeadersSupplier(),
        grpcSenderConfig.getRetryPolicy(),
        grpcSenderConfig.getSslContext(),
        grpcSenderConfig.getTrustManager(),
        grpcSenderConfig.getExecutorService());
  }
}
