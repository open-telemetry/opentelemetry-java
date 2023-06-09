/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import com.google.auto.value.AutoValue;
import java.time.Duration;

/** Configuration for exporter exponential retry policy. */
@AutoValue
public abstract class RetryPolicy {

  private static final RetryPolicy DEFAULT = new RetryPolicyBuilder().build();

  RetryPolicy() {}

  /** Return the default {@link RetryPolicy}. */
  public static RetryPolicy getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link RetryPolicyBuilder} to construct a {@link RetryPolicy}. */
  public static RetryPolicyBuilder builder() {
    return new RetryPolicyBuilder();
  }

  /** Returns the max number of attempts, including the original request. */
  public abstract int getMaxAttempts();

  /** Returns the initial backoff. */
  public abstract Duration getInitialBackoff();

  /** Returns the max backoff. */
  public abstract Duration getMaxBackoff();

  /** Returns the backoff multiplier. */
  public abstract double getBackoffMultiplier();

  static RetryPolicy create(
      int maxAttempts, Duration initialBackoff, Duration maxBackoff, double backoffMultiplier) {
    return new AutoValue_RetryPolicy(maxAttempts, initialBackoff, maxBackoff, backoffMultiplier);
  }
}
