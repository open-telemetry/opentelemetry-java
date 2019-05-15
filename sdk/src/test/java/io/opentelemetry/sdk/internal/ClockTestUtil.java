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

final class ClockTestUtil {
  static final int NANOS_PER_SECOND = 1000 * 1000 * 1000;
  static final int NANOS_PER_MILLI = 1000 * 1000;

  static Timestamp createTimestamp(long seconds, int nanos) {
    return Timestamp.newBuilder().setSeconds(seconds).setNanos(nanos).build();
  }

  private ClockTestUtil() {}
}
