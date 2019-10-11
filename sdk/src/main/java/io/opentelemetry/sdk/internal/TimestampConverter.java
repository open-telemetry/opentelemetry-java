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

import io.opentelemetry.common.Timestamp;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides a mechanism for converting {@link System#nanoTime() nanoTime} values to
 * {@link Timestamp}.
 */
@Immutable
public class TimestampConverter {

  static final long NANOS_PER_SECOND = 1_000_000_000;
  static final long NANOS_PER_MILLI = 1_000_000;

  private final Timestamp timestamp;
  private final long nanoTime;

  /**
   * Returns a {@code TimestampConverter} initialized to now.
   *
   * @param clock the {@code Clock} to be used to read the current time.
   * @return a {@code TimestampConverter} initialized to now.
   */
  public static TimestampConverter now(Clock clock) {
    return new TimestampConverter(clock.now(), clock.nowNanos());
  }

  /**
   * Converts a {@link System#nanoTime() nanoTime} value to {@link Timestamp}.
   *
   * @param nanoTime value to convert.
   * @return the {@code Timestamp} representation of the {@code time}.
   */
  public Timestamp convertNanoTime(long nanoTime) {
    long deltaNanos = nanoTime - this.nanoTime;

    long seconds = timestamp.getSeconds() + (deltaNanos / NANOS_PER_SECOND);
    long nanos = timestamp.getNanos() + (deltaNanos % NANOS_PER_SECOND);

    if (nanos >= NANOS_PER_SECOND) {
      seconds += nanos / NANOS_PER_SECOND;
      nanos = nanos % NANOS_PER_SECOND;
    }
    return Timestamp.create(seconds, (int) nanos);
  }

  private TimestampConverter(Timestamp timestamp, long nanoTime) {
    this.timestamp = timestamp;
    this.nanoTime = nanoTime;
  }
}
