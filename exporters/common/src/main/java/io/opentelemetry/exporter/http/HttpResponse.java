/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.http;

import io.opentelemetry.exporter.marshal.MessageWriter;
import java.util.function.Consumer;
import javax.annotation.concurrent.Immutable;

/**
 * A HTTP response.
 *
 * @see HttpSender#send(MessageWriter, Consumer, Consumer)
 */
@Immutable
public interface HttpResponse {

  /** The HTTP status code. */
  int getStatusCode();

  /** The HTTP status message. */
  String getStatusMessage();

  /** The HTTP response body bytes. */
  @SuppressWarnings("mutable")
  default byte[] getResponseBody() {
    return new byte[0];
  }
}
