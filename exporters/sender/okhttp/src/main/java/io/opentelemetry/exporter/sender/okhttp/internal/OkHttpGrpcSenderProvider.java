/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.grpc.Channel;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.grpc.GrpcSenderProvider;
import io.opentelemetry.exporter.internal.grpc.MarshalerServiceStub;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.jetbrains.annotations.Nullable;

/**
 * {@link GrpcSender} SPI implementation for {@link OkHttpGrpcSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OkHttpGrpcSenderProvider implements GrpcSenderProvider {

  @Override
  public <T extends Marshaler> GrpcSender<T> createSender(
      URI endpoint,
      String endpointPath,
      boolean compressionEnabled,
      long timeoutNanos,
      Supplier<Map<String, String>> headersSupplier,
      @Nullable Object managedChannel,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager) {
    return new OkHttpGrpcSender<>(
        endpoint.resolve(endpointPath).toString(),
        compressionEnabled,
        timeoutNanos,
        headersSupplier,
        retryPolicy,
        sslContext,
        trustManager);
  }
}
