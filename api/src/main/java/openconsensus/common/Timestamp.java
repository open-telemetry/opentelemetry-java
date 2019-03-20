/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.common;

import static openconsensus.common.TimeUtils.MAX_NANOS;
import static openconsensus.common.TimeUtils.MAX_SECONDS;
import static openconsensus.common.TimeUtils.MILLIS_PER_SECOND;
import static openconsensus.common.TimeUtils.NANOS_PER_MILLI;
import static openconsensus.common.TimeUtils.NANOS_PER_SECOND;

import com.google.auto.value.AutoValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.concurrent.Immutable;

/**
 * A representation of an instant in time. The instant is the number of nanoseconds after the number
 * of seconds since the Unix Epoch.
 *
 * <p>Use {@code Tracing.getClock().now()} to get the current timestamp since epoch
 * (1970-01-01T00:00:00Z).
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Timestamp implements Comparable<Timestamp> {

  Timestamp() {}

  /**
   * Creates a new timestamp from given seconds and nanoseconds.
   *
   * @param seconds Represents seconds of UTC time since Unix epoch 1970-01-01T00:00:00Z. Must be
   *     from from 0001-01-01T00:00:00Z to 9999-12-31T23:59:59Z inclusive.
   * @param nanos Non-negative fractions of a second at nanosecond resolution. Negative second
   *     values with fractions must still have non-negative nanos values that count forward in time.
   *     Must be from 0 to 999,999,999 inclusive.
   * @return new {@code Timestamp} with specified fields.
   * @throws IllegalArgumentException if the arguments are out of range.
   * @since 0.1.0
   */
  public static Timestamp create(long seconds, int nanos) {
    if (seconds < -MAX_SECONDS) {
      throw new IllegalArgumentException(
          "'seconds' is less than minimum (" + -MAX_SECONDS + "): " + seconds);
    }
    if (seconds > MAX_SECONDS) {
      throw new IllegalArgumentException(
          "'seconds' is greater than maximum (" + MAX_SECONDS + "): " + seconds);
    }
    if (nanos < 0) {
      throw new IllegalArgumentException("'nanos' is less than zero: " + nanos);
    }
    if (nanos > MAX_NANOS) {
      throw new IllegalArgumentException(
          "'nanos' is greater than maximum (" + MAX_NANOS + "): " + nanos);
    }
    return new AutoValue_Timestamp(seconds, nanos);
  }

  /**
   * Creates a new timestamp from the given milliseconds.
   *
   * @param epochMilli the timestamp represented in milliseconds since epoch.
   * @return new {@code Timestamp} with specified fields.
   * @throws IllegalArgumentException if the number of milliseconds is out of the range that can be
   *     represented by {@code Timestamp}.
   * @since 0.1.0
   */
  public static Timestamp fromMillis(long epochMilli) {
    long secs = floorDiv(epochMilli, MILLIS_PER_SECOND);
    int mos = (int) floorMod(epochMilli, MILLIS_PER_SECOND);
    return create(secs, (int) (mos * NANOS_PER_MILLI)); // Safe int * NANOS_PER_MILLI
  }

  /**
   * Returns the number of seconds since the Unix Epoch represented by this timestamp.
   *
   * @return the number of seconds since the Unix Epoch.
   * @since 0.1.0
   */
  public abstract long getSeconds();

  /**
   * Returns the number of nanoseconds after the number of seconds since the Unix Epoch represented
   * by this timestamp.
   *
   * @return the number of nanoseconds after the number of seconds since the Unix Epoch.
   * @since 0.1.0
   */
  public abstract int getNanos();

  /**
   * Returns a {@code Timestamp} calculated as this {@code Timestamp} plus some number of
   * nanoseconds.
   *
   * @param nanosToAdd the nanos to add, positive or negative.
   * @return the calculated {@code Timestamp}. For invalid inputs, a {@code Timestamp} of zero is
   *     returned.
   * @throws ArithmeticException if numeric overflow occurs.
   * @since 0.1.0
   */
  public Timestamp addNanos(long nanosToAdd) {
    return plus(0, nanosToAdd);
  }

  /**
   * Returns a {@code Timestamp} calculated as this {@code Timestamp} plus some {@code Duration}.
   *
   * @param duration the {@code Duration} to add.
   * @return a {@code Timestamp} with the specified {@code Duration} added.
   * @since 0.1.0
   */
  public Timestamp addDuration(Duration duration) {
    return plus(duration.getSeconds(), duration.getNanos());
  }

  /**
   * Returns a {@link Duration} calculated as: {@code this - timestamp}.
   *
   * @param timestamp the {@code Timestamp} to subtract.
   * @return the calculated {@code Duration}. For invalid inputs, a {@code Duration} of zero is
   *     returned.
   * @since 0.1.0
   */
  public Duration subtractTimestamp(Timestamp timestamp) {
    long durationSeconds = getSeconds() - timestamp.getSeconds();
    int durationNanos = getNanos() - timestamp.getNanos();
    if (durationSeconds < 0 && durationNanos > 0) {
      durationSeconds += 1;
      durationNanos = (int) (durationNanos - NANOS_PER_SECOND);
    } else if (durationSeconds > 0 && durationNanos < 0) {
      durationSeconds -= 1;
      durationNanos = (int) (durationNanos + NANOS_PER_SECOND);
    }
    return Duration.create(durationSeconds, durationNanos);
  }

  /**
   * Compares this {@code Timestamp} to the specified {@code Timestamp}.
   *
   * @param otherTimestamp the other {@code Timestamp} to compare to, not {@code null}.
   * @return the comparator value: zero if equal, negative if this timestamp happens before
   *     otherTimestamp, positive if after.
   * @throws NullPointerException if otherTimestamp is {@code null}.
   */
  @Override
  public int compareTo(Timestamp otherTimestamp) {
    int cmp = TimeUtils.compareLongs(getSeconds(), otherTimestamp.getSeconds());
    if (cmp != 0) {
      return cmp;
    }
    return TimeUtils.compareLongs(getNanos(), otherTimestamp.getNanos());
  }

  // Returns a Timestamp with the specified duration added.
  private Timestamp plus(long secondsToAdd, long nanosToAdd) {
    if ((secondsToAdd | nanosToAdd) == 0) {
      return this;
    }
    long epochSec = TimeUtils.checkedAdd(getSeconds(), secondsToAdd);
    epochSec = TimeUtils.checkedAdd(epochSec, nanosToAdd / NANOS_PER_SECOND);
    nanosToAdd = nanosToAdd % NANOS_PER_SECOND;
    long nanoAdjustment = getNanos() + nanosToAdd; // safe int + NANOS_PER_SECOND
    return ofEpochSecond(epochSec, nanoAdjustment);
  }

  // Returns a Timestamp calculated using seconds from the epoch and nanosecond fraction of
  // second (arbitrary number of nanoseconds).
  private static Timestamp ofEpochSecond(long epochSecond, long nanoAdjustment) {
    long secs = TimeUtils.checkedAdd(epochSecond, floorDiv(nanoAdjustment, NANOS_PER_SECOND));
    int nos = (int) floorMod(nanoAdjustment, NANOS_PER_SECOND);
    return create(secs, nos);
  }

  // Returns the result of dividing x by y rounded using floor.
  private static long floorDiv(long x, long y) {
    return BigDecimal.valueOf(x).divide(BigDecimal.valueOf(y), 0, RoundingMode.FLOOR).longValue();
  }

  // Returns the floor modulus "x - (floorDiv(x, y) * y)"
  private static long floorMod(long x, long y) {
    return x - floorDiv(x, y) * y;
  }
}
