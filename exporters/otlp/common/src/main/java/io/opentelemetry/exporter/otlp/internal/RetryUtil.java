/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import static io.grpc.Status.Code.ABORTED;
import static io.grpc.Status.Code.CANCELLED;
import static io.grpc.Status.Code.DATA_LOSS;
import static io.grpc.Status.Code.DEADLINE_EXCEEDED;
import static io.grpc.Status.Code.OUT_OF_RANGE;
import static io.grpc.Status.Code.RESOURCE_EXHAUSTED;
import static io.grpc.Status.Code.UNAVAILABLE;

import io.grpc.Status;
import io.opentelemetry.RetryConfig;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import net.jodah.failsafe.RetryPolicy;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryUtil {

  private static final Set<Status.Code> RETRYABLE_CODES =
      Collections.unmodifiableSet(
          new HashSet<>(
              Arrays.asList(
                  CANCELLED,
                  DEADLINE_EXCEEDED,
                  RESOURCE_EXHAUSTED,
                  ABORTED,
                  OUT_OF_RANGE,
                  UNAVAILABLE,
                  DATA_LOSS)));

  public static Set<Status.Code> retryableStatusCodes() {
    return RETRYABLE_CODES;
  }

  public static boolean hasRetryableStatusCode(Throwable throwable) {
    return RETRYABLE_CODES.contains(Status.fromThrowable(throwable).getCode());
  }

  /** Convert the retry config to a retry policy. */
  public static <T> RetryPolicy<T> toRetryPolicy(RetryConfig retryConfig) {
    ExponentialBackoffConfig exponentialBackoffConfig = (ExponentialBackoffConfig) retryConfig;
    return new RetryPolicy<T>()
        .withBackoff(
            exponentialBackoffConfig.getInitialBackoff().toMillis(),
            exponentialBackoffConfig.getMaxBackoff().toMillis(),
            ChronoUnit.MILLIS,
            exponentialBackoffConfig.getBackoffMultiplier())
        .withMaxRetries((int) exponentialBackoffConfig.getMaxRetries())
        .withJitter(exponentialBackoffConfig.getJitter());
  }

  private RetryUtil() {}
}
