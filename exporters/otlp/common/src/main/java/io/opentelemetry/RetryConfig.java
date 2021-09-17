/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry;

import io.opentelemetry.exporter.otlp.internal.ExponentialBackoffConfig;
import java.time.Duration;

@SuppressWarnings("InterfaceWithOnlyStatics")
public interface RetryConfig {

  /**
   * Create a retry config representing an exponential backoff policy.
   *
   * @param maxAttempts the max number of retries
   * @param initialBackoff the wait period before attempting the first retry
   * @param maxBackoff the maximum wait period before retry attempts
   * @param backoffMultiplier the multiplication factor used to compute the delay between successive
   *     failures
   * @param jitter the duration to randomly vary retry delays by
   */
  static RetryConfig exponentialBackoff(
      long maxAttempts,
      Duration initialBackoff,
      Duration maxBackoff,
      double backoffMultiplier,
      Duration jitter) {
    return ExponentialBackoffConfig.create(
        maxAttempts, initialBackoff, maxBackoff, backoffMultiplier, jitter);
  }
}
