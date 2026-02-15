/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.grpc;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.export.Compressor;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoValue
public abstract class ImmutableGrpcSenderConfig implements ExtendedGrpcSenderConfig {

  @SuppressWarnings("TooManyParameters")
  public static ImmutableGrpcSenderConfig create(
      URI endpoint,
      String fullMethodName,
      @Nullable Compressor compressor,
      Duration timeoutNanos,
      Duration connectTimeoutNanos,
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
