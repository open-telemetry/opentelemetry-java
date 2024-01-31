/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal.http;

import io.opentelemetry.exporter.internal.marshal.Marshaler;
import io.opentelemetry.sdk.common.CompletableResultCode;
import java.io.IOException;
import java.util.function.Consumer;

/**
 * An abstraction for sending HTTP requests and handling responses.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see HttpExporter
 * @see HttpExporterBuilder
 */
public interface HttpSender {

  /**
   * Send an HTTP request, including any retry attempts. {@code onResponse} is called with the HTTP
   * response, either a success response or a error response after retries. {@code onError} is
   * called when the request could not be executed due to cancellation, connectivity problems, or
   * timeout.
   *
   * @param marshaler the request body marshaler
   * @param contentLength the request body content length
   * @param onResponse the callback to invoke with the HTTP response
   * @param onError the callback to invoke when the HTTP request could not be executed
   */
  void send(
      Marshaler marshaler,
      int contentLength,
      Consumer<Response> onResponse,
      Consumer<Throwable> onError);

  /** Shutdown the sender. */
  CompletableResultCode shutdown();

  /** The HTTP response. */
  interface Response {

    /** The HTTP status code. */
    int statusCode();

    /** The HTTP status message. */
    String statusMessage();

    /** The HTTP response body. */
    byte[] responseBody() throws IOException;
  }
}
