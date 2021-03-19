/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import static java.util.concurrent.TimeUnit.NANOSECONDS;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.IntStream;

/** Will limit the number of log messages emitted, so as not to spam when problems are happening. */
public class ThrottlingLogger {
  private static final int MAXIMUM_LOGS_PER_MINUTE_BEFORE_THROTTLE = 5;
  private final Logger delegate;
  private final Clock clock;

  private final AtomicLong timeOfLastEmittedLog = new AtomicLong(0);
  private final AtomicBoolean throttled = new AtomicBoolean(false);

  private final ConcurrentLinkedDeque<Long> window = new ConcurrentLinkedDeque<>();

  public ThrottlingLogger(Logger delegate) {
    this(delegate, SystemClock.getInstance());
  }

  // visible for testing
  ThrottlingLogger(Logger delegate, Clock clock) {
    this.delegate = delegate;
    this.clock = clock;

    // pre-populate with a full 5 to make the logic easier below
    IntStream.range(0, MAXIMUM_LOGS_PER_MINUTE_BEFORE_THROTTLE).forEach(i -> window.push(0L));
  }

  public void log(Level level, String message) {
    long now = clock.now();
    long eldestTimestamp = window.removeLast();
    window.addFirst(now);

    long windowDurationMinutes = minutesBetween(now, eldestTimestamp);

    if (throttled.get()) {
      if (minutesBetween(now, timeOfLastEmittedLog.get()) >= 1) {
        doLog(level, message, now);
      }
      return;
    }

    if (windowDurationMinutes >= 1) {
      doLog(level, message, now);
      return;
    }

    delegate.log(
        level, "Too many log messages detected. Will only log once per minute from now on.");
    throttled.set(true);
  }

  private void doLog(Level level, String message, long now) {
    delegate.log(level, message);
    timeOfLastEmittedLog.set(now);
  }

  private static long minutesBetween(long now, long then) {
    return NANOSECONDS.toMinutes(now - then);
  }

  public boolean isLoggable(Level level) {
    return delegate.isLoggable(level);
  }
}
