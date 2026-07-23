/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.internal;

import io.opentelemetry.sdk.common.export.GrpcStatusCode;
import java.time.Duration;
import java.time.Instant;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.OptionalLong;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryUtil {

  private static final Set<String> RETRYABLE_GRPC_STATUS_CODES;
  private static final Set<Integer> RETRYABLE_HTTP_STATUS_CODES =
      Collections.unmodifiableSet(new HashSet<>(Arrays.asList(429, 502, 503, 504)));

  static {
    Set<Integer> retryableGrpcStatusCodes = new HashSet<>();
    retryableGrpcStatusCodes.add(GrpcStatusCode.CANCELLED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.DEADLINE_EXCEEDED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.RESOURCE_EXHAUSTED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.ABORTED.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.OUT_OF_RANGE.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.UNAVAILABLE.getValue());
    retryableGrpcStatusCodes.add(GrpcStatusCode.DATA_LOSS.getValue());
    RETRYABLE_GRPC_STATUS_CODES =
        Collections.unmodifiableSet(
            retryableGrpcStatusCodes.stream().map(Object::toString).collect(Collectors.toSet()));
  }

  private RetryUtil() {}

  /** Returns the retryable gRPC status codes. */
  public static Set<String> retryableGrpcStatusCodes() {
    return RETRYABLE_GRPC_STATUS_CODES;
  }

  /** Returns the retryable HTTP status codes. */
  public static Set<Integer> retryableHttpResponseCodes() {
    return RETRYABLE_HTTP_STATUS_CODES;
  }

  /** Returns whether the status code represents OTLP/HTTP throttling. */
  public static boolean isOtlpHttpThrottleStatusCode(int statusCode) {
    return statusCode == 429 || statusCode == 503;
  }

  /**
   * Returns the delay specified by a {@code Retry-After} header, or empty if the value is absent or
   * malformed.
   */
  public static OptionalLong retryAfterNanos(@Nullable String retryAfter) {
    return retryAfterNanos(retryAfter, Instant.now());
  }

  static OptionalLong retryAfterNanos(@Nullable String retryAfter, Instant now) {
    if (retryAfter == null) {
      return OptionalLong.empty();
    }

    try {
      long delaySeconds = Long.parseLong(retryAfter);
      if (delaySeconds < 0) {
        return OptionalLong.empty();
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
