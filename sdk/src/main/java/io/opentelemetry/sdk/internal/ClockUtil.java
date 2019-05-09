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

import com.google.protobuf.Timestamp;

class ClockUtil {
  static final int MILLIS_PER_SECOND = 1000;
  static final int NANOS_PER_SECOND = 1000 * 1000 * 1000;
  static final int NANOS_PER_MILLI = 1000 * 1000;

  static Timestamp fromNanos(long nanos) {
    return Timestamp.newBuilder()
        .setSeconds(floorDiv(nanos, ClockUtil.NANOS_PER_SECOND))
        .setNanos((int) floorMod(nanos, ClockUtil.NANOS_PER_SECOND))
        .build();
  }

  static Timestamp fromMillis(long epochMilli) {
    long secs = ClockUtil.floorDiv(epochMilli, ClockUtil.MILLIS_PER_SECOND);
    long mos = ClockUtil.floorMod(epochMilli, ClockUtil.MILLIS_PER_SECOND);
    return createTimestamp(secs, (int) (mos * ClockUtil.NANOS_PER_MILLI)); // Safe
  }

  static Timestamp createTimestamp(long seconds, int nanos) {
    return Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build();
  }

  private static long floorDiv(long x, long y) {
    long r = x / y;
    // if the signs are different and modulo not zero, round down
    if ((x ^ y) < 0 && (r * y != x)) {
      r--;
    }
    return r;
  }

  private static long floorMod(long x, long y) {
    return x - floorDiv(x, y) * y;
  }
}
