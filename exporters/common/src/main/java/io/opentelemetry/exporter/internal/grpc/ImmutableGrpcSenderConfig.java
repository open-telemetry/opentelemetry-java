/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.compressor.Compressor;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

@AutoValue
abstract class ImmutableGrpcSenderConfig implements ExtendedGrpcSenderConfig {

  @SuppressWarnings("TooManyParameters")
  static ImmutableGrpcSenderConfig create(
      URI endpoint,
      String fullMethodName,
      @Nullable Compressor compressor,
      long timeoutNanos,
      long connectTimeoutNanos,
      Supplier<Map<String, List<String>>> headersSupplier,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService,
      @Nullable Object managedChannel) {
    return new AutoValue_ImmutableGrpcSenderConfig(
        endpoint,
        fullMethodName,
        compressor,
        timeoutNanos,
        connectTimeoutNanos,
        headersSupplier,
        retryPolicy,
        sslContext,
        trustManager,
        executorService,
        managedChannel);
  }
}
