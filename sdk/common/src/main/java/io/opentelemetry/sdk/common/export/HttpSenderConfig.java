/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.OutputStream;
import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.function.Consumer;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;
import javax.net.ssl.SSLContext;
import javax.net.ssl.X509TrustManager;

/**
 * Configuration for {@link HttpSender} implementations, provided via {@link
 * HttpSenderProvider#createSender(HttpSenderConfig)}.
 *
 * @since 1.59.0
 */
@Immutable
public interface HttpSenderConfig {

  /** The fully qualified endpoint to send to, including scheme and path. */
  URI getEndpoint();

  /** The payload content type to set as the {@code Content-Type} header. */
  String getContentType();

  /**
   * The compressor, or {@code null} if no compression is used. If present, {@link
   * Compressor#compress(OutputStream)} must be applied to {@link
   * MessageWriter#writeMessage(OutputStream)} when {@link HttpSender#send(MessageWriter, Consumer,
   * Consumer)} is called and {@link Compressor#getEncoding()} must be set as the {@code
   * Content-Encoding} header.
   */
  @Nullable
  Compressor getCompressor();

  /**
   * The max duration allowed to send a request, including resolving DNS, connecting, writing the
   * request, reading the response, and any retries via {@link #getRetryPolicy()}.
   */
  Duration getTimeout();

  /** The max duration allowed to connect to a target host. */
  Duration getConnectTimeout();

  /**
   * Additional headers that must be appended to every request. The resulting {@link Supplier} must
   * be invoked for each request.
   */
  Supplier<Map<String, List<String>>> getHeadersSupplier();

  /** The proxy options, or {@code null} if no proxy is used. */
  @Nullable
  ProxyOptions getProxyOptions();

  /** The retry policy, or {@code null} if retry is disabled. */
  @Nullable
  RetryPolicy getRetryPolicy();

  /**
   * The SSL context to use, or {@code null} if the system default is used. If non-null, {@link
   * #getTrustManager()} will also be non-null.
   */
  @Nullable
  SSLContext getSslContext();

  /**
   * The trust manager to use, or {@code null} if the system default is used. If non-null, {@link
   * #getSslContext()} will also be non-null.
   */
  @Nullable
  X509TrustManager getTrustManager();

  /**
   * The executor service used to execute any asynchronous processing, or {@code null} if the sender
   * default executor service should be used.
   */
  @Nullable
  ExecutorService getExecutorService();
}
