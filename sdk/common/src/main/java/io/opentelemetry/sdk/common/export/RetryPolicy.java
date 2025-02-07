/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.export;

import static io.opentelemetry.api.internal.Utils.checkArgument;

import com.google.auto.value.AutoValue;
import java.io.IOException;
import java.time.Duration;
import java.util.function.Predicate;
import javax.annotation.Nullable;

/**
 * Configuration for exporter exponential retry policy.
 *
 * @since 1.28.0
 */
@AutoValue
public abstract class RetryPolicy {

  private static final int DEFAULT_MAX_ATTEMPTS = 5;

  @SuppressWarnings("StronglyTypeTime")
  private static final int DEFAULT_INITIAL_BACKOFF_SECONDS = 1;

  @SuppressWarnings("StronglyTypeTime")
  private static final int DEFAULT_MAX_BACKOFF_SECONDS = 5;

  private static final double DEFAULT_BACKOFF_MULTIPLIER = 1.5;

  private static final RetryPolicy DEFAULT = RetryPolicy.builder().build();

  RetryPolicy() {}

  /** Return the default {@link RetryPolicy}. */
  public static RetryPolicy getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link RetryPolicyBuilder} to construct a {@link RetryPolicy}. */
  public static RetryPolicyBuilder builder() {
    return new AutoValue_RetryPolicy.Builder()
        .setMaxAttempts(DEFAULT_MAX_ATTEMPTS)
        .setInitialBackoff(Duration.ofSeconds(DEFAULT_INITIAL_BACKOFF_SECONDS))
        .setMaxBackoff(Duration.ofSeconds(DEFAULT_MAX_BACKOFF_SECONDS))
        .setBackoffMultiplier(DEFAULT_BACKOFF_MULTIPLIER);
  }

  /**
   * Returns a {@link RetryPolicyBuilder} reflecting configuration values for this {@link
   * RetryPolicy}.
   *
   * @since 1.29.0
   */
  public abstract RetryPolicyBuilder toBuilder();

  /** Returns the max number of attempts, including the original request. */
  public abstract int getMaxAttempts();

  /** Returns the initial backoff. */
  public abstract Duration getInitialBackoff();

  /** Returns the max backoff. */
  public abstract Duration getMaxBackoff();

  /** Returns the backoff multiplier. */
  public abstract double getBackoffMultiplier();

  /**
   * Returns the predicate used to determine if an attempt which failed exceptionally should be
   * retried, or {@code null} if the exporter specific default predicate should be used.
   *
   * @since 1.47.0
   */
  @Nullable
  public abstract Predicate<IOException> getRetryExceptionPredicate();

  /** Builder for {@link RetryPolicy}. */
  @AutoValue.Builder
  public abstract static class RetryPolicyBuilder {

    RetryPolicyBuilder() {}

    /**
     * Set the maximum number of attempts, including the original request. Must be greater than 1
     * and less than 6. Defaults to {@value DEFAULT_MAX_ATTEMPTS}.
     */
    public abstract RetryPolicyBuilder setMaxAttempts(int maxAttempts);

    /**
     * Set the initial backoff. Must be greater than 0. Defaults to {@value
     * DEFAULT_INITIAL_BACKOFF_SECONDS} seconds.
     */
    public abstract RetryPolicyBuilder setInitialBackoff(Duration initialBackoff);

    /**
     * Set the maximum backoff. Must be greater than 0. Defaults to {@value
     * DEFAULT_MAX_BACKOFF_SECONDS} seconds.
     */
    public abstract RetryPolicyBuilder setMaxBackoff(Duration maxBackoff);

    /**
     * Set the backoff multiplier. Must be greater than 0.0. Defaults to {@value
     * DEFAULT_BACKOFF_MULTIPLIER}.
     */
    public abstract RetryPolicyBuilder setBackoffMultiplier(double backoffMultiplier);

    /**
     * Set the predicate used to determine if an attempt which failed exceptionally should be
     * retried. By default, an exporter specific default predicate should be used.
     *
     * @since 1.47.0
     */
    public abstract RetryPolicyBuilder setRetryExceptionPredicate(
        Predicate<IOException> retryExceptionPredicate);

    abstract RetryPolicy autoBuild();

    /** Build and return a {@link RetryPolicy} with the values of this builder. */
    public RetryPolicy build() {
      RetryPolicy retryPolicy = autoBuild();
      checkArgument(
          retryPolicy.getMaxAttempts() > 1 && retryPolicy.getMaxAttempts() < 6,
          "maxAttempts must be greater than 1 and less than 6");
      checkArgument(
          retryPolicy.getInitialBackoff().toNanos() > 0, "initialBackoff must be greater than 0");
      checkArgument(retryPolicy.getMaxBackoff().toNanos() > 0, "maxBackoff must be greater than 0");
      checkArgument(
          retryPolicy.getBackoffMultiplier() > 0, "backoffMultiplier must be greater than 0");

      return retryPolicy;
    }
  }
}
