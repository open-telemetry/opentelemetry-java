/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.grpc.GrpcSender;
import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * A Builder for the {@link GrpcSender} which uses OkHttp instead of grpc-java.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class OkHttpGrpcSenderBuilder<T extends Marshaler> {
  private final String endpoint;
  private final long timeoutNanos;
  private final long connectTimeoutNanos;
  private final Supplier<Map<String, List<String>>> headersSupplier;
  @Nullable private Compressor compressor;
  @Nullable private RetryPolicy retryPolicy;
  @Nullable private SSLContext sslContext;
  @Nullable private X509TrustManager trustManager;
  @Nullable private ExecutorService executorService;

  public OkHttpGrpcSenderBuilder(
      String endpoint,
      long timeoutNanos,
      long connectTimeoutNanos,
      Supplier<Map<String, List<String>>> headersSupplier) {
    this.endpoint = endpoint;
    this.timeoutNanos = timeoutNanos;
    this.connectTimeoutNanos = connectTimeoutNanos;
    this.headersSupplier = headersSupplier;
  }

  public OkHttpGrpcSenderBuilder<T> setCompressor(@Nullable Compressor compressor) {
    this.compressor = compressor;
    return this;
  }

  public OkHttpGrpcSenderBuilder<T> setRetryPolicy(@Nullable RetryPolicy retryPolicy) {
    this.retryPolicy = retryPolicy;
    return this;
  }

  public OkHttpGrpcSenderBuilder<T> setSslContext(@Nullable SSLContext sslContext) {
    this.sslContext = sslContext;
    return this;
  }

  public OkHttpGrpcSenderBuilder<T> setTrustManager(@Nullable X509TrustManager trustManager) {
    this.trustManager = trustManager;
    return this;
  }

  public OkHttpGrpcSenderBuilder<T> setExecutorService(@Nullable ExecutorService executorService) {
    this.executorService = executorService;
    return this;
  }

  public OkHttpGrpcSender<T> createOkHttpGrpcSender() {
    return new OkHttpGrpcSender<>(
        endpoint,
        compressor,
        timeoutNanos,
        connectTimeoutNanos,
        headersSupplier,
        retryPolicy,
        sslContext,
        trustManager,
        executorService);
  }
}
