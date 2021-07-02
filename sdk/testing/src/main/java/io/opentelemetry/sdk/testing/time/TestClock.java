/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.testing.time;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/** A mutable {@link Clock} that allows the time to be set for testing. */
@ThreadSafe
public final class TestClock implements Clock {

  @GuardedBy("this")
  private long currentEpochNanos;

  private TestClock(long epochNanos) {
    currentEpochNanos = epochNanos;
  }

  /**
   * Creates a clock initialized to a constant non-zero time.
   *
   * @return a clock initialized to a constant non-zero time.
   */
  public static TestClock create() {
    // Set Time to Tuesday, May 7, 2019 12:00:00 AM GMT-07:00 DST
    return create(Instant.ofEpochMilli(1_557_212_400_000L));
  }

  /** Creates a clock with the given time. */
  public static TestClock create(Instant instant) {
    return new TestClock(toNanos(instant));
  }

  /** Sets the current time. */
  public synchronized void setTime(Instant instant) {
    currentEpochNanos = toNanos(instant);
  }

  /** Advances the time and mutates this instance. */
  public synchronized void advance(Duration duration) {
    advance(duration.toNanos(), TimeUnit.NANOSECONDS);
  }

  /** Advances the time and mutates this instance. */
  public synchronized void advance(long duration, TimeUnit unit) {
    currentEpochNanos += unit.toNanos(duration);
  }

  @Override
  public synchronized long now() {
    return currentEpochNanos;
  }

  @Override
  public synchronized long nanoTime() {
    return currentEpochNanos;
  }

  private static long toNanos(Instant instant) {
    return TimeUnit.SECONDS.toNanos(instant.getEpochSecond()) + instant.getNano();
  }
}
