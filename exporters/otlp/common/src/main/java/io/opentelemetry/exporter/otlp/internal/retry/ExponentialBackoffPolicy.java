/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import java.time.Duration;
import java.util.Random;

class ExponentialBackoffPolicy implements RetryPolicy {

  private static final Random RANDOM = new Random();

  private final long maxRetries;
  private final long initialBackoffMillis;
  private final long maxBackoffMillis;
  private final double backoffMultiplier;

  ExponentialBackoffPolicy(
      long maxRetries, Duration initialBackoff, Duration maxBackoff, double backoffMultiplier) {
    this.maxRetries = maxRetries;
    this.initialBackoffMillis = initialBackoff.toMillis();
    this.maxBackoffMillis = maxBackoff.toMillis();
    this.backoffMultiplier = backoffMultiplier;
  }

  @Override
  public boolean shouldRetry(RetryContext retryContext) {
    return retryContext.getAttemptCount() <= maxRetries;
  }

  @Override
  public long retryDelayMillis(RetryContext retryContext) {
    int attemptCount = retryContext.getAttemptCount();
    if (attemptCount == 0) {
      return RANDOM.nextInt((int) initialBackoffMillis);
    }
    long targetBackoff =
        (long) (initialBackoffMillis * Math.pow(backoffMultiplier, attemptCount - 1));
    return RANDOM.nextInt((int) Math.min(targetBackoff, maxBackoffMillis));
  }
}
