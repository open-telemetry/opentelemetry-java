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

import static io.opentelemetry.sdk.internal.TimestampConverter.NANOS_PER_MILLI;
import static io.opentelemetry.sdk.internal.TimestampConverter.NANOS_PER_SECOND;

import io.opentelemetry.sdk.common.Timestamp;
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
  private Timestamp currentTimestamp;

  private TestClock(Timestamp timestamp) {
    currentTimestamp = timestamp;
  }

  /**
   * Creates a clock initialized to a constant non-zero time.
   *
   * @return a clock initialized to a constant non-zero time.
   * @since 0.1.0
   */
  public static TestClock create() {
    // Set Time to Tuesday, May 7, 2019 12:00:00 AM GMT-07:00 DST
    return create(Timestamp.fromMillis(1_557_212_400_000L));
  }

  /**
   * Creates a clock with the given time.
   *
   * @param timestamp the initial time.
   * @return a new {@code TestClock} with the given time.
   * @since 0.1.0
   */
  public static TestClock create(Timestamp timestamp) {
    return new TestClock(timestamp);
  }

  /**
   * Sets the time.
   *
   * @param timestamp the new time.
   * @since 0.1.0
   */
  public synchronized void setTime(Timestamp timestamp) {
    currentTimestamp = timestamp;
  }

  /**
   * Advances the time by millis and mutates this instance.
   *
   * @param millis the increase in time.
   * @since 0.1.0
   */
  public synchronized void advanceMillis(long millis) {
    long incomingSeconds = millis / 1000;
    long remainingMillis = millis % 1000;
    long remainingNanos = remainingMillis * NANOS_PER_MILLI;

    long newSeconds = incomingSeconds + currentTimestamp.getSeconds();
    long newNanos = remainingNanos + currentTimestamp.getNanos();

    if (newNanos >= NANOS_PER_SECOND) {
      newSeconds += newNanos / NANOS_PER_SECOND;
      newNanos = newNanos % NANOS_PER_SECOND;
    }
    currentTimestamp = Timestamp.create(newSeconds, (int) newNanos);
  }

  @Override
  public synchronized Timestamp now() {
    return currentTimestamp;
  }

  @Override
  public synchronized long nowNanos() {
    return (currentTimestamp.getSeconds() * NANOS_PER_SECOND) + currentTimestamp.getNanos();
  }
}
