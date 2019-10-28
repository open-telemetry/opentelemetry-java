/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A mutable {@link Clock} that allows the time to be set for testing.
 *
 * @since 0.1.0
 */
@ThreadSafe
public class TestClock implements Clock {

  @GuardedBy("this")
  private long currentEpochNanos;

  private TestClock(long epochNanos) {
    currentEpochNanos = epochNanos;
  }

  /**
   * Creates a clock initialized to a constant non-zero time.
   *
   * @return a clock initialized to a constant non-zero time.
   * @since 0.1.0
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
   * @since 0.1.0
   */
  public static TestClock create(long epochNanos) {
    return new TestClock(epochNanos);
  }

  /**
   * Sets the time.
   *
   * @param epochNanos the new time.
   * @since 0.1.0
   */
  public synchronized void setTime(long epochNanos) {
    currentEpochNanos = epochNanos;
  }

  /**
   * Advances the time by millis and mutates this instance.
   *
   * @param millis the increase in time.
   * @since 0.1.0
   */
  public synchronized void advanceMillis(long millis) {
    long nanos = TimeUnit.MILLISECONDS.toNanos(millis);
    currentEpochNanos += nanos;
  }

  /**
   * Advances the time by nanos and mutates this instance.
   *
   * @param nanos the increase in time.
   * @since 0.1.0
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
