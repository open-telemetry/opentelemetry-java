/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.function.Consumer;

/**
 * An abstraction for executing gRPC calls, allowing for implementations backed by different client
 * libraries.
 *
 * <p>While this interface is public, implementing a custom sender is generally not recommended. The
 * {@code opentelemetry-java} project provides built-in implementations that cover virtually all
 * cases.
 *
 * @see GrpcSenderProvider
 */
public interface GrpcSender {

  /**
   * Execute a gRPC unary call, including any retry attempts. {@code onResponse} is called with the
   * gRPC response, either a success response or an error response after retries. {@code onError} is
   * called when the call could not be executed due to cancellation, connectivity problems, or
   * timeout.
   *
   * @param messageWriter the message writer
   * @param onResponse the callback to invoke with the gRPC response
   * @param onError the callback to invoke when the gRPC call could not be executed
   */
  void send(
      MessageWriter messageWriter, Consumer<GrpcResponse> onResponse, Consumer<Throwable> onError);

  /** Shutdown the sender. */
  CompletableResultCode shutdown();
}
