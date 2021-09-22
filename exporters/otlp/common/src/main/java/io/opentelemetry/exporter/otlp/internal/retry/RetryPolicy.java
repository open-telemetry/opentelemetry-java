/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import java.time.Duration;

public interface RetryPolicy {

  /** Create a retry config representing an exponential backoff policy. */
  static RetryPolicy exponentialBackoff(
      long maxAttempts, Duration initialBackoff, Duration maxBackoff, double backoffMultiplier) {
    return new ExponentialBackoffPolicy(maxAttempts, initialBackoff, maxBackoff, backoffMultiplier);
  }

  /** Return a retry policy which disables retrying. */
  static RetryPolicy noRetry() {
    return new RetryPolicy() {
      @Override
      public boolean shouldRetry(RetryContext retryContext) {
        return false;
      }

      @Override
      public long retryDelayMillis(RetryContext retryContext) {
        return 0;
      }
    };
  }

  /** Determine if a retry should be attempted based on the context. */
  boolean shouldRetry(RetryContext retryContext);

  /**
   * Determine the number of millis to delay before retrying. Only called after {@link
   * #shouldRetry(RetryContext)} returns true.
   */
  long retryDelayMillis(RetryContext retryContext);
}
