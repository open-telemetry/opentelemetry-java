/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export.impl;

import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.OptionalLong;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * HTTP utilities for exporter transport implementations.
 *
 * <p>This class is not intended for use by application developers. Its API is stable and will not
 * be changed or removed in a backwards-incompatible manner.
 */
public final class HttpExportUtil {

  private HttpExportUtil() {}

  /**
   * Returns the delay specified by a {@code Retry-After} header, or empty if the value is absent or
   * malformed.
   *
   * @param retryAfter the value of the {@code Retry-After} header, or {@code null}
   * @return the delay in nanoseconds, or {@link OptionalLong#empty()}
   */
  public static OptionalLong retryAfterNanos(@Nullable String retryAfter) {
    return retryAfterNanos(retryAfter, Instant.now());
  }

  // Visible for testing
  static OptionalLong retryAfterNanos(@Nullable String retryAfter, Instant now) {
    if (retryAfter == null) {
      return OptionalLong.empty();
    }

    try {
      long delaySeconds = Long.parseLong(retryAfter);
      // RFC 9110 delay-seconds is 1*DIGIT. Long.parseLong also accepts signs and non-ASCII digits.
      for (int i = 0; i < retryAfter.length(); i++) {
        char digit = retryAfter.charAt(i);
        if (digit < '0' || digit > '9') {
          return OptionalLong.empty();
        }
      }
      return OptionalLong.of(TimeUnit.SECONDS.toNanos(delaySeconds));
    } catch (NumberFormatException ignored) {
      // Fall through and try the HTTP-date form.
    }

    try {
      Instant retryAfterInstant =
          ZonedDateTime.parse(retryAfter, DateTimeFormatter.RFC_1123_DATE_TIME).toInstant();
      long delayNanos = Duration.between(now, retryAfterInstant).toNanos();
      return OptionalLong.of(Math.max(0, delayNanos));
    } catch (DateTimeParseException | ArithmeticException ignored) {
      return OptionalLong.empty();
    }
  }
}
