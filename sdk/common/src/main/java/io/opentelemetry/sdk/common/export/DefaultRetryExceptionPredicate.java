/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.util.Locale;
import java.util.function.Predicate;

public class DefaultRetryExceptionPredicate implements Predicate<IOException> {
  @Override
  public boolean test(IOException e) {
    if (e instanceof SocketTimeoutException) {
      String message = e.getMessage();
      // Connect timeouts can produce SocketTimeoutExceptions with no message, or with "connect
      // timed out", or timeout
      if (message == null) {
        return true;
      }
      message = message.toLowerCase(Locale.ROOT);
      return message.contains("connect timed out") || message.contains("timeout");
    } else if (e instanceof ConnectException) {
      // Exceptions resemble: java.net.ConnectException: Failed to connect to
      // localhost/[0:0:0:0:0:0:0:1]:62611
      return true;
    }
    return false;
  }
}
