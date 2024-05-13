/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.exporter.internal.auth.Authenticator;
import io.opentelemetry.exporter.internal.compression.Compressor;
import io.opentelemetry.sdk.common.export.ProxyOptions;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * A service provider interface (SPI) for providing {@link HttpSender}s backed by different HTTP
 * client libraries.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public interface HttpSenderProvider {

  /** Returns a {@link HttpSender} configured with the provided parameters. */
  @SuppressWarnings("TooManyParameters")
  HttpSender createSender(
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
      @Nullable X509TrustManager trustManager);
}
