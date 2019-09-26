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
import com.google.protobuf.util.Durations;
import com.google.protobuf.util.Timestamps;
import io.opentelemetry.sdk.trace.export.SpanData;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides a mechanism for converting {@link System#nanoTime() nanoTime} values to
 * {@link Timestamp}.
 */
@Immutable
public class TimestampConverter {
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
  public Timestamp convertNanoTimeProto(long nanoTime) {
    return Timestamps.add(timestamp, Durations.fromNanos(nanoTime - this.nanoTime));
  }

  private TimestampConverter(Timestamp timestamp, long nanoTime) {
    this.timestamp = timestamp;
    this.nanoTime = nanoTime;
  }

  /**
   * Converts a {@link System#nanoTime() nanoTime} value to {@link SpanData.Timestamp}.
   *
   * @param nanoTime value to convert.
   * @return the {@code SpanData.Timestamp} representation of the {@code time}.
   */
  public SpanData.Timestamp convertNanoTime(long nanoTime) {
    // todo: implement this without going through the protobuf intermediary.
    Timestamp protoVersion = convertNanoTimeProto(nanoTime);
    return SpanData.Timestamp.create(protoVersion.getSeconds(), protoVersion.getNanos());
  }
}
