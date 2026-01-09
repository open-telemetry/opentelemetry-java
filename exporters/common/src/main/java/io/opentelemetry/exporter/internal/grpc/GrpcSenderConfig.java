/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import com.google.auto.value.AutoValue;
import io.grpc.Channel;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoValue
@Immutable
public abstract class GrpcSenderConfig<T extends Marshaler> {

  @SuppressWarnings("TooManyParameters")
  public static <T extends Marshaler> GrpcSenderConfig<T> create(
      URI endpoint,
      String endpointPath,
      @Nullable Compressor compressor,
      long timeoutNanos,
      long connectTimeoutNanos,
      Supplier<Map<String, List<String>>> headersSupplier,
      @Nullable Object managedChannel,
      Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>> stubFactory,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService) {
    return new AutoValue_GrpcSenderConfig<>(
        endpoint,
        endpointPath,
        compressor,
        timeoutNanos,
        connectTimeoutNanos,
        headersSupplier,
        managedChannel,
        stubFactory,
        retryPolicy,
        sslContext,
        trustManager,
        executorService);
  }

  public abstract URI getEndpoint();

  public abstract String getEndpointPath();

  @Nullable
  public abstract Compressor getCompressor();

  public abstract long getTimeoutNanos();

  public abstract long getConnectTimeoutNanos();

  public abstract Supplier<Map<String, List<String>>> getHeadersSupplier();

  @Nullable
  public abstract Object getManagedChannel();

  public abstract Supplier<BiFunction<Channel, String, MarshalerServiceStub<T, ?, ?>>>
      getStubFactory();

  @Nullable
  public abstract RetryPolicy getRetryPolicy();

  @Nullable
  public abstract SSLContext getSslContext();

  @Nullable
  public abstract X509TrustManager getTrustManager();

  @Nullable
  public abstract ExecutorService getExecutorService();
}
