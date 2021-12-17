/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import static io.opentelemetry.api.internal.Utils.checkArgument;
import static java.util.Objects.requireNonNull;

import java.time.Duration;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class RetryPolicyBuilder {

  private static final int DEFAULT_MAX_ATTEMPTS = 5;
  private static final Duration DEFAULT_INITIAL_BACKOFF = Duration.ofSeconds(1);
  private static final Duration DEFAULT_MAX_BACKOFF = Duration.ofSeconds(5);
  private static final double DEFAULT_BACKOFF_MULTIPLIER = 1.5;

  private int maxAttempts = DEFAULT_MAX_ATTEMPTS;
  private Duration initialBackoff = DEFAULT_INITIAL_BACKOFF;
  private Duration maxBackoff = DEFAULT_MAX_BACKOFF;
  private double backoffMultiplier = DEFAULT_BACKOFF_MULTIPLIER;

  RetryPolicyBuilder() {}

  /**
   * Set the maximum number of attempts, including the original request. Must be greater than 1 and
   * less than 6.
   */
  public RetryPolicyBuilder setMaxAttempts(int maxAttempts) {
    checkArgument(
        maxAttempts > 1 && maxAttempts < 6, "maxAttempts must be greater than 1 and less than 6");
    this.maxAttempts = maxAttempts;
    return this;
  }

  /** Set the initial backoff. Must be greater than 0. */
  public RetryPolicyBuilder setInitialBackoff(Duration initialBackoff) {
    requireNonNull(initialBackoff, "initialBackoff");
    checkArgument(initialBackoff.toNanos() > 0, "initialBackoff must be greater than 0");
    this.initialBackoff = initialBackoff;
    return this;
  }

  /** Set the maximum backoff. Must be greater than 0. */
  public RetryPolicyBuilder setMaxBackoff(Duration maxBackoff) {
    requireNonNull(maxBackoff, "maxBackoff");
    checkArgument(maxBackoff.toNanos() > 0, "maxBackoff must be greater than 0");
    this.maxBackoff = maxBackoff;
    return this;
  }

  /** Set the backoff multiplier. Must be greater than 0.0. */
  public RetryPolicyBuilder setBackoffMultiplier(double backoffMultiplier) {
    checkArgument(backoffMultiplier > 0, "backoffMultiplier must be greater than 0");
    this.backoffMultiplier = backoffMultiplier;
    return this;
  }

  /** Build and return a {@link RetryPolicy} with the values of this builder. */
  public RetryPolicy build() {
    return RetryPolicy.create(maxAttempts, initialBackoff, maxBackoff, backoffMultiplier);
  }
}
