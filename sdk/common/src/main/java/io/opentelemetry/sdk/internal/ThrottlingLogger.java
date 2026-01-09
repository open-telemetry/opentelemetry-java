/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static java.util.concurrent.TimeUnit.MINUTES;

import io.opentelemetry.sdk.common.Clock;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Will limit the number of log messages emitted, so as not to spam when problems are happening.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public class ThrottlingLogger {
  private static final double DEFAULT_RATE_LIMIT = 5;
  private static final double DEFAULT_THROTTLED_RATE_LIMIT = 1;
  private static final TimeUnit DEFAULT_RATE_TIME_UNIT = MINUTES;

  private final Logger delegate;
  private final AtomicBoolean throttled = new AtomicBoolean(false);
  private final RateLimiter fastRateLimiter;
  private final RateLimiter throttledRateLimiter;

  private final double rateLimit;
  private final double throttledRateLimit;
  private final TimeUnit rateTimeUnit;

  /** Create a new logger which will enforce a max number of messages per minute. */
  public ThrottlingLogger(Logger delegate) {
    this(delegate, Clock.getDefault());
  }

  /** Alternate way to create logger that allows setting custom intervals and units. * */
  public ThrottlingLogger(
      Logger delegate, double rateLimit, double throttledRateLimit, TimeUnit rateTimeUnit) {
    this(delegate, Clock.getDefault(), rateLimit, throttledRateLimit, rateTimeUnit);
  }

  // visible for testing
  ThrottlingLogger(Logger delegate, Clock clock) {
    this(delegate, clock, DEFAULT_RATE_LIMIT, DEFAULT_THROTTLED_RATE_LIMIT, DEFAULT_RATE_TIME_UNIT);
  }

  ThrottlingLogger(
      Logger delegate,
      Clock clock,
      double rateLimit,
      double throttledRateLimit,
      TimeUnit rateTimeUnit) {
    this.delegate = delegate;
    this.rateLimit = rateLimit;
    this.throttledRateLimit = throttledRateLimit;
    this.rateTimeUnit = rateTimeUnit;
    this.fastRateLimiter =
        new RateLimiter(this.rateLimit / this.rateTimeUnit.toSeconds(1), this.rateLimit, clock);
    this.throttledRateLimiter =
        new RateLimiter(
            this.throttledRateLimit / this.rateTimeUnit.toSeconds(1),
            this.throttledRateLimit,
            clock);
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
      throttledRateLimiter.trySpend(throttledRateLimit);
      String timeUnitString = rateTimeUnit.toString().toLowerCase(Locale.ROOT);
      String throttleMessage =
          String.format(
              Locale.ROOT,
              "Too many log messages detected. Will only log %.0f time(s) per %s from now on.",
              throttledRateLimit,
              timeUnitString.substring(0, timeUnitString.length() - 1));
      delegate.log(level, throttleMessage);
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
   * Returns whether the current wrapped logger is set to log at the given level.
   *
   * @return true if the logger set to log at the requested level.
   */
  public boolean isLoggable(Level level) {
    return delegate.isLoggable(level);
  }
}
