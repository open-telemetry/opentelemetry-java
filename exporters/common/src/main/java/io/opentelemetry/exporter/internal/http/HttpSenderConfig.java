/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import com.google.auto.value.AutoValue;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
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
public abstract class HttpSenderConfig {

  @SuppressWarnings("TooManyParameters")
  public static HttpSenderConfig create(
      String endpoint,
      @Nullable Compressor compressor,
      boolean exportAsJson,
      String contentType,
      long timeoutNanos,
      long connectTimeoutNanos,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable ProxyOptions proxyOptions,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager,
      @Nullable ExecutorService executorService) {
    return new AutoValue_HttpSenderConfig(
        endpoint,
        compressor,
        exportAsJson,
        contentType,
        timeoutNanos,
        connectTimeoutNanos,
        headerSupplier,
        proxyOptions,
        retryPolicy,
        sslContext,
        trustManager,
        executorService);
  }

  public abstract String getEndpoint();

  @Nullable
  public abstract Compressor getCompressor();

  public abstract boolean getExportAsJson();

  public abstract String getContentType();

  public abstract long getTimeoutNanos();

  public abstract long getConnectTimeoutNanos();

  public abstract Supplier<Map<String, List<String>>> getHeadersSupplier();

  @Nullable
  public abstract ProxyOptions getProxyOptions();

  @Nullable
  public abstract RetryPolicy getRetryPolicy();

  @Nullable
  public abstract SSLContext getSslContext();

  @Nullable
  public abstract X509TrustManager getTrustManager();

  @Nullable
  public abstract ExecutorService getExecutorService();
}
