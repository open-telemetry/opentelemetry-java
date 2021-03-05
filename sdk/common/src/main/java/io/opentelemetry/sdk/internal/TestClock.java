/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.internal.GuardedBy;
import io.opentelemetry.sdk.common.Clock;
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
    return create(TimeUnit.MILLISECONDS.toNanos(1_557_212_400_000L));
  }

  /**
   * Creates a clock with the given time.
   *
   * @param epochNanos the initial time in nanos since epoch.
   * @return a new {@code TestClock} with the given time.
   */
  public static TestClock create(long epochNanos) {
    return new TestClock(epochNanos);
  }

  /**
   * Sets the time.
   *
   * @param epochNanos the new time.
   */
  public synchronized void setTime(long epochNanos) {
    currentEpochNanos = epochNanos;
  }

  /**
   * Advances the time by millis and mutates this instance.
   *
   * @param millis the increase in time.
   */
  public synchronized void advanceMillis(long millis) {
    long nanos = TimeUnit.MILLISECONDS.toNanos(millis);
    currentEpochNanos += nanos;
  }

  /**
   * Advances the time by nanos and mutates this instance.
   *
   * @param nanos the increase in time.
   */
  public synchronized void advanceNanos(long nanos) {
    currentEpochNanos += nanos;
  }

  @Override
  public synchronized long now() {
    return currentEpochNanos;
  }

  @Override
  public synchronized long nanoTime() {
    return currentEpochNanos;
  }
}
