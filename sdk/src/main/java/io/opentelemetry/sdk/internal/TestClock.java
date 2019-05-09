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

import com.google.common.math.LongMath;
import com.google.protobuf.Timestamp;
import javax.annotation.concurrent.GuardedBy;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Clock} that allows the time to be set for testing.
 *
 * @since 0.1.0
 */
@ThreadSafe
public class TestClock implements Clock {
  @GuardedBy("this")
  private long currentNanos;

  private TestClock(Timestamp time) {
    currentNanos = getNanos(time);
  }

  /**
   * Creates a clock initialized to a constant non-zero time.
   *
   * @return a clock initialized to a constant non-zero time.
   * @since 0.1.0
   */
  public static TestClock create() {
    // Set Time to Tuesday, May 7, 2019 12:00:00 AM GMT-07:00 DST
    return create(ClockUtil.createTimestamp(1557212400, 0));
  }

  /**
   * Creates a clock with the given time.
   *
   * @param time the initial time.
   * @return a new {@code TestClock} with the given time.
   * @since 0.1.0
   */
  public static TestClock create(Timestamp time) {
    return new TestClock(time);
  }

  /**
   * Sets the time.
   *
   * @param time the new time.
   * @since 0.1.0
   */
  public synchronized void setTime(Timestamp time) {
    currentNanos = getNanos(time);
  }

  /**
   * Advances the time by millis.
   *
   * @param millis the increase in time.
   * @since 0.1.0
   */
  public synchronized void advanceMillis(long millis) {
    currentNanos = LongMath.checkedAdd(currentNanos, millis * ClockUtil.NANOS_PER_MILLI);
  }

  @Override
  public synchronized Timestamp now() {
    return ClockUtil.fromNanos(currentNanos);
  }

  @Override
  public synchronized long nowNanos() {
    return currentNanos;
  }

  // Converts Timestamp into nanoseconds since time 0 and throws an exception if it overflows.
  private static long getNanos(Timestamp time) {
    return LongMath.checkedAdd(
        LongMath.checkedMultiply(time.getSeconds(), ClockUtil.NANOS_PER_SECOND), time.getNanos());
  }
}
