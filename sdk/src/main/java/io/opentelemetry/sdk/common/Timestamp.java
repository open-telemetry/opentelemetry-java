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

package io.opentelemetry.sdk.common;

import com.google.auto.value.AutoValue;
import java.math.BigDecimal;
import java.math.RoundingMode;
import javax.annotation.concurrent.Immutable;

/**
 * A representation of an instant in time. The instant is the number of nanoseconds after the number
 * of seconds since the Unix Epoch.
 *
 * <p>Defined here instead of using {@code Instant} because the API needs to be Java 1.7 compatible.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Timestamp {
  private static final long MAX_SECONDS = 315576000000L;
  private static final int MAX_NANOS = 999999999;
  private static final long MILLIS_PER_SECOND = 1000L;
  private static final long NANOS_PER_MILLI = 1000 * 1000;

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

  // Returns the result of dividing x by y rounded using floor.
  private static long floorDiv(long x, long y) {
    return BigDecimal.valueOf(x).divide(BigDecimal.valueOf(y), 0, RoundingMode.FLOOR).longValue();
  }

  // Returns the floor modulus "x - (floorDiv(x, y) * y)"
  private static long floorMod(long x, long y) {
    return x - floorDiv(x, y) * y;
  }
}
