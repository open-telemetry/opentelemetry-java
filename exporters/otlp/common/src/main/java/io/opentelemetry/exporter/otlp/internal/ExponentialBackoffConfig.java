/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal;

import com.google.auto.value.AutoValue;
import io.opentelemetry.RetryConfig;
import java.time.Duration;
import javax.annotation.concurrent.Immutable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
@AutoValue
@Immutable
public abstract class ExponentialBackoffConfig implements RetryConfig {

  public static ExponentialBackoffConfig create(
      long maxRetries,
      Duration initialBackoff,
      Duration maxBackoff,
      double backoffMultiplier,
      Duration jitter) {
    return new AutoValue_ExponentialBackoffConfig(
        maxRetries, initialBackoff, maxBackoff, backoffMultiplier, jitter);
  }

  /** The maximum number of retries. */
  abstract long getMaxRetries();

  /** The wait period before attempting the first retry. */
  abstract Duration getInitialBackoff();

  /** The maximum wait period before retry attempts. */
  abstract Duration getMaxBackoff();

  /** The multiplication factor used to compute the delay between successive failures. */
  abstract double getBackoffMultiplier();

  /** The duration to randomly vary retry delays by. */
  abstract Duration getJitter();

  ExponentialBackoffConfig() {}
}
