/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.sender.okhttp.internal;

import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.exporter.internal.http.HttpSender;
import io.opentelemetry.exporter.internal.http.HttpSenderProvider;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;
import org.jetbrains.annotations.Nullable;

/**
 * {@link HttpSender} SPI implementation for {@link OkHttpHttpSender}.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class OkHttpHttpSenderProvider implements HttpSenderProvider {

  @Override
  public HttpSender createSender(
      String endpoint,
      @Nullable Compressor compressor,
      boolean exportAsJson,
      String contentType,
      long timeoutNanos,
      long connectTimeout,
      Supplier<Map<String, List<String>>> headerSupplier,
      @Nullable ProxyOptions proxyOptions,
      @Nullable Authenticator authenticator,
      @Nullable RetryPolicy retryPolicy,
      @Nullable SSLContext sslContext,
      @Nullable X509TrustManager trustManager) {
    return new OkHttpHttpSender(
        endpoint,
        compressor,
        exportAsJson,
        contentType,
        timeoutNanos,
        connectTimeout,
        headerSupplier,
        proxyOptions,
        authenticator,
        retryPolicy,
        sslContext,
        trustManager);
  }
}
