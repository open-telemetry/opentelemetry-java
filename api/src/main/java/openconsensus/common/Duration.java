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

import com.google.auto.value.AutoValue;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

/**
 * Represents a signed, fixed-length span of time represented as a count of seconds and fractions of
 * seconds at nanosecond resolution. It is independent of any calendar and concepts like "day" or
 * "month". Range is approximately +-10,000 years.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Duration implements Comparable<Duration> {

  /**
   * Creates a new time duration from given seconds and nanoseconds.
   *
   * @param seconds Signed seconds of the span of time. Must be from -315,576,000,000 to
   *     +315,576,000,000 inclusive.
   * @param nanos Signed fractions of a second at nanosecond resolution of the span of time.
   *     Durations less than one second are represented with a 0 `seconds` field and a positive or
   *     negative `nanos` field. For durations of one second or more, a non-zero value for the
   *     `nanos` field must be of the same sign as the `seconds` field. Must be from -999,999,999 to
   *     +999,999,999 inclusive.
   * @return new {@code Duration} with specified fields.
   * @throws IllegalArgumentException if the arguments are out of range or have inconsistent sign.
   * @since 0.1.0
   */
  public static Duration create(long seconds, int nanos) {
    if (seconds < -MAX_SECONDS) {
      throw new IllegalArgumentException(
          "'seconds' is less than minimum (" + -MAX_SECONDS + "): " + seconds);
    }
    if (seconds > MAX_SECONDS) {
      throw new IllegalArgumentException(
          "'seconds' is greater than maximum (" + MAX_SECONDS + "): " + seconds);
    }
    if (nanos < -MAX_NANOS) {
      throw new IllegalArgumentException(
          "'nanos' is less than minimum (" + -MAX_NANOS + "): " + nanos);
    }
    if (nanos > MAX_NANOS) {
      throw new IllegalArgumentException(
          "'nanos' is greater than maximum (" + MAX_NANOS + "): " + nanos);
    }
    if ((seconds < 0 && nanos > 0) || (seconds > 0 && nanos < 0)) {
      throw new IllegalArgumentException(
          "'seconds' and 'nanos' have inconsistent sign: seconds=" + seconds + ", nanos=" + nanos);
    }
    return new AutoValue_Duration(seconds, nanos);
  }

  /**
   * Creates a new {@code Duration} from given milliseconds.
   *
   * @param millis the duration in milliseconds.
   * @return a new {@code Duration} from given milliseconds.
   * @throws IllegalArgumentException if the number of milliseconds is out of the range that can be
   *     represented by {@code Duration}.
   * @since 0.1.0
   */
  public static Duration fromMillis(long millis) {
    long seconds = millis / MILLIS_PER_SECOND;
    int nanos = (int) (millis % MILLIS_PER_SECOND * NANOS_PER_MILLI);
    return Duration.create(seconds, nanos);
  }

  /**
   * Converts a {@link Duration} to milliseconds.
   *
   * @return the milliseconds representation of this {@code Duration}.
   * @since 0.1.0
   */
  public long toMillis() {
    return TimeUnit.SECONDS.toMillis(getSeconds()) + TimeUnit.NANOSECONDS.toMillis(getNanos());
  }

  /**
   * Returns the number of seconds in the {@code Duration}.
   *
   * @return the number of seconds in the {@code Duration}.
   * @since 0.1.0
   */
  public abstract long getSeconds();

  /**
   * Returns the number of nanoseconds in the {@code Duration}.
   *
   * @return the number of nanoseconds in the {@code Duration}.
   * @since 0.1.0
   */
  public abstract int getNanos();

  /**
   * Compares this {@code Duration} to the specified {@code Duration}.
   *
   * @param otherDuration the other {@code Duration} to compare to, not {@code null}.
   * @return the comparator value: zero if equal, negative if this duration is smaller than
   *     otherDuration, positive if larger.
   * @throws NullPointerException if otherDuration is {@code null}.
   */
  @Override
  public int compareTo(Duration otherDuration) {
    int cmp = TimeUtils.compareLongs(getSeconds(), otherDuration.getSeconds());
    if (cmp != 0) {
      return cmp;
    }
    return TimeUtils.compareLongs(getNanos(), otherDuration.getNanos());
  }

  Duration() {}
}
