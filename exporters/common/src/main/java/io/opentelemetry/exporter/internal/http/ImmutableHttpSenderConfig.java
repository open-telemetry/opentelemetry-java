/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import com.google.auto.value.AutoValue;
import io.opentelemetry.sdk.common.export.Compressor;
import io.opentelemetry.sdk.common.export.HttpSenderConfig;
import io.opentelemetry.sdk.common.export.ProxyOptions;
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

@AutoValue
abstract class ImmutableHttpSenderConfig implements HttpSenderConfig {

  @SuppressWarnings("TooManyParameters")
  static HttpSenderConfig create(
      URI endpoint,
      String contentType,
      @Nullable Compressor compressor,
      Duration timeoutNanos,
      Duration connectTimeoutNanos,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable ProxyOptions proxyOptions,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService) {
    return new AutoValue_ImmutableHttpSenderConfig(
        endpoint,
        contentType,
        compressor,
        timeoutNanos,
        connectTimeoutNanos,
        headerSupplier,
        proxyOptions,
        retryPolicy,
        sslContext,
        trustManager,
        executorService);
  }
}
