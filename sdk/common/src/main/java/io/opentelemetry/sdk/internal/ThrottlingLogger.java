/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/** Will limit the number of log messages emitted, so as not to spam when problems are happening. */
public class ThrottlingLogger {
//  private static final int RATE_LIMIT = 5;
//  private static final int THROTTLED_RATE_LIMIT = 1;
//  private static final TimeUnit rateTimeUnit = MINUTES;

  private final Logger delegate;
//  private final Clock clock;

  // this window tracks the number of logs messages per time unit. It holds the times of `log`
  // calls.
//  private final Deque<Long> window = new ConcurrentLinkedDeque<>();
  private final AtomicBoolean throttled = new AtomicBoolean(false);
  private final RateLimiter rateLimiter;

  /** Create a new logger which will enforce a max number of messages per minute. */
  public ThrottlingLogger(Logger delegate) {
    this(delegate, SystemClock.getInstance());
  }

  // visible for testing
  ThrottlingLogger(Logger delegate, Clock clock) {
    this.delegate = delegate;
//    this.clock = clock;
    this.rateLimiter = new RateLimiter(1d/60d,4d, clock);

    // pre-populate to make the logic easier below
//    IntStream.range(0, RATE_LIMIT).forEach(i -> window.push(0L));
  }

  /** Log a message at a level. */
  public void log(Level level, String message) {
    log(level, message, null);
  }

  /** Log a message at a level with a throwable. */
  public void log(Level level, String message, @Nullable Throwable throwable) {
    boolean ok = rateLimiter.checkCredit(1d);
    if (!ok) {
      if (throttled.compareAndSet(false, true)) {
        delegate.log(
            level, "Too many log messages detected. Will only log once per minute from now on.");
        doLog(level, message, throwable);
      }
    } else {
      throttled.set(false);
      doLog(level, message, throwable);
    }
//    long now = clock.now();
//    window.addFirst(now);
//    long eldestTimestamp = window.removeLast();
//
//    long windowDurationMinutes = minutesBetween(now, eldestTimestamp);
//
//    if (windowDurationMinutes >= 1) {
//      if (throwable != null) {
//        delegate.log(level, message, throwable);
//      } else {
//        delegate.log(level, message);
//      }
//      return;
//    }
//
//    if (throttled.compareAndSet(false, true)) {
//      // shrink the queue down to only the throttled number of items
//      IntStream.range(0, RATE_LIMIT - THROTTLED_RATE_LIMIT).forEach(i -> window.removeLast());
//      delegate.log(
//          level, "Too many log messages detected. Will only log once per minute from now on.");
//    }
  }

  private void doLog(Level level, String message, Throwable throwable) {
    if (throwable != null) {
      delegate.log(level, message, throwable);
    } else {
      delegate.log(level, message);
    }
  }

  public boolean isLoggable(Level level) {
    return delegate.isLoggable(level);
  }

//  private static long minutesBetween(long now, long then) {
//    return rateTimeUnit.convert(now - then, NANOSECONDS);
//  }
}
