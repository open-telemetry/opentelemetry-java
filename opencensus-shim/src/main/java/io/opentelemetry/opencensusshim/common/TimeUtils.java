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

package io.opentelemetry.opencensusshim.common;

import java.math.BigInteger;

/** Util class for {@link Timestamp} and {@link Duration}. */
final class TimeUtils {
  static final long MAX_SECONDS = 315576000000L;
  static final int MAX_NANOS = 999999999;
  static final long MILLIS_PER_SECOND = 1000L;
  static final long NANOS_PER_MILLI = 1000 * 1000;
  static final long NANOS_PER_SECOND = NANOS_PER_MILLI * MILLIS_PER_SECOND;

  private TimeUtils() {}

  /**
   * Compares two longs. This functionality is provided by {@code Long.compare(long, long)} in Java
   * 7.
   */
  static int compareLongs(long x, long y) {
    if (x < y) {
      return -1;
    } else if (x == y) {
      return 0;
    } else {
      return 1;
    }
  }

  private static final BigInteger MAX_LONG_VALUE = BigInteger.valueOf(Long.MAX_VALUE);
  private static final BigInteger MIN_LONG_VALUE = BigInteger.valueOf(Long.MIN_VALUE);

  /**
   * Adds two longs and throws an {@link ArithmeticException} if the result overflows. This
   * functionality is provided by {@code Math.addExact(long, long)} in Java 8.
   */
  static long checkedAdd(long x, long y) {
    BigInteger sum = BigInteger.valueOf(x).add(BigInteger.valueOf(y));
    if (sum.compareTo(MAX_LONG_VALUE) > 0 || sum.compareTo(MIN_LONG_VALUE) < 0) {
      throw new ArithmeticException("Long sum overflow: x=" + x + ", y=" + y);
    }
    return x + y;
  }
}
