/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.grpc;

import io.opentelemetry.exporter.compressor.Compressor;
import io.opentelemetry.exporter.marshal.MessageWriter;
import io.opentelemetry.sdk.common.export.RetryPolicy;
import java.io.OutputStream;
import java.net.URI;
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
 * Configuration for {@link GrpcSender} implementations, provided via {@link
 * GrpcSenderProvider#createSender(GrpcSenderConfig)}.
 */
@Immutable
public interface GrpcSenderConfig {

  /**
   * The gRPC endpoint to send to, including scheme. Omits path, which must be obtained from {@link
   * #getFullMethodName()}.
   */
  URI getEndpoint();

  /**
   * The fully qualified gRPC method name, e.g. {@code
   * opentelemetry.proto.collector.trace.v1.TraceService/Export}.
   */
  String getFullMethodName();

  /**
   * The compressor, or {@code null} if no compression is used. If present, {@link
   * Compressor#compress(OutputStream)} must be applied to {@link
   * MessageWriter#writeMessage(OutputStream)} when {@link GrpcSender#send(MessageWriter, Consumer,
   * Consumer)} is called and {@link Compressor#getEncoding()} must be set as the {@code
   * grpc-encoding}.
   */
  @Nullable
  Compressor getCompressor();

  /**
   * The max time in nanoseconds allowed to send a request, including resolving DNS, connecting,
   * writing the request, reading the response, and any retries via {@link #getRetryPolicy()}.
   */
  long getTimeoutNanos();

  /** The max time in nanoseconds allowed to connect to a target host. */
  long getConnectTimeoutNanos();

  /**
   * Additional headers that must be appended to every request. The resulting {@link Supplier} must
   * be invoked for each request.
   */
  Supplier<Map<String, List<String>>> getHeadersSupplier();

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
