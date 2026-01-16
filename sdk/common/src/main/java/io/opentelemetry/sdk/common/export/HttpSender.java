/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import io.opentelemetry.sdk.common.CompletableResultCode;
import java.util.function.Consumer;

/**
 * An abstraction for executing HTTP requests, allowing for implementations backed by different
 * client libraries.
 *
 * <p>While this interface is public, implementing a custom sender is generally not recommended. The
 * {@code opentelemetry-java} project provides built-in implementations that cover virtually all
 * cases.
 *
 * @see HttpSenderProvider
 */
public interface HttpSender {

  /**
   * Send an HTTP request, including any retry attempts. {@code onResponse} is called with the HTTP
   * response, either a success response or an error response after retries. {@code onError} is
   * called when the request could not be executed due to cancellation, connectivity problems, or
   * timeout.
   *
   * @param messageWriter the request body message writer
   * @param onResponse the callback to invoke with the HTTP response
   * @param onError the callback to invoke when the HTTP request could not be executed
   */
  void send(
      MessageWriter messageWriter, Consumer<HttpResponse> onResponse, Consumer<Throwable> onError);

  /** Shutdown the sender. */
  CompletableResultCode shutdown();
}
