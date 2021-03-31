/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static java.util.concurrent.TimeUnit.MINUTES;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Will limit the number of log messages emitted, so as not to spam when problems are happening. */
public class ThrottlingLogger {
  private static final double RATE_LIMIT = 5;
  private static final double THROTTLED_RATE_LIMIT = 1;
  private static final TimeUnit rateTimeUnit = MINUTES;

  private final Logger delegate;
  private final AtomicBoolean throttled = new AtomicBoolean(false);
  private final RateLimiter fastRateLimiter;
  private final RateLimiter throttledRateLimiter;

  /** Create a new logger which will enforce a max number of messages per minute. */
  public ThrottlingLogger(Logger delegate) {
    this(delegate, SystemClock.getInstance());
  }

  // visible for testing
  ThrottlingLogger(Logger delegate, Clock clock) {
    this.delegate = delegate;
    this.fastRateLimiter =
        new RateLimiter(RATE_LIMIT / rateTimeUnit.toSeconds(1), RATE_LIMIT, clock);
    this.throttledRateLimiter =
        new RateLimiter(RATE_LIMIT / rateTimeUnit.toSeconds(1), THROTTLED_RATE_LIMIT, clock);
  }

  /** Log a message at the given level. */
  public void log(Level level, String message) {
    log(level, message, null);
  }

  /** Log a message at the given level with a throwable. */
  public void log(Level level, String message, @Nullable Throwable throwable) {
    if (!isLoggable(level)) {
      return;
    }
    if (throttled.get()) {
      if (throttledRateLimiter.trySpend(1.0)) {
        doLog(level, message, throwable);
      }
      return;
    }

    if (fastRateLimiter.trySpend(1.0)) {
      doLog(level, message, throwable);
      return;
    }

    if (throttled.compareAndSet(false, true)) {
      // spend the balance in the throttled one, so that it starts at zero.
      throttledRateLimiter.trySpend(THROTTLED_RATE_LIMIT);
      delegate.log(
          level, "Too many log messages detected. Will only log once per minute from now on.");
      doLog(level, message, throwable);
    }
  }

  private void doLog(Level level, String message, @Nullable Throwable throwable) {
    if (throwable != null) {
      delegate.log(level, message, throwable);
    } else {
      delegate.log(level, message);
    }
  }

  /**
   * Check if the current wrapped logger is set to log at the given level.
   *
   * @return true if the logger set to log at the requested level.
   */
  public boolean isLoggable(Level level) {
    return delegate.isLoggable(level);
  }
}
