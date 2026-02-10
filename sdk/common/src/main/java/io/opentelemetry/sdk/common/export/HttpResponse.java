/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.util.function.Consumer;
import javax.annotation.concurrent.Immutable;

/**
 * A HTTP response.
 *
 * @see HttpSender#send(MessageWriter, Consumer, Consumer)
 * @since 1.59.0
 */
@Immutable
public interface HttpResponse {

  /** The HTTP status code. */
  int getStatusCode();

  /** The HTTP status message. */
  String getStatusMessage();

  /** The HTTP response body bytes. */
  @SuppressWarnings("mutable")
  byte[] getResponseBody();
}
