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

import io.opentelemetry.sdk.common.Clock;
import javax.annotation.concurrent.Immutable;

/**
 * This class provides a mechanism for calculating the epoch time using {@link System#nanoTime()}
 * and a reference epoch timestamp.
 *
 * <p>This is needed because Java has millisecond granularity for epoch times and tracing events are
 * recorded more often.
 *
 * <p>This clock needs to be re-created periodically in order to re-sync with the kernel clock, and
 * it is not recommended to use only one instance for a very long period of time.
 */
@Immutable
public final class MonotonicClock implements Clock {
  private final Clock clock;
  private final long epochNanos;
  private final long nanoTime;

  private MonotonicClock(Clock clock, long epochNanos, long nanoTime) {
    this.clock = clock;
    this.epochNanos = epochNanos;
    this.nanoTime = nanoTime;
  }

  /**
   * Returns a {@code MonotonicClock}.
   *
   * @param clock the {@code Clock} to be used to read the current epoch time and nanoTime.
   * @return a {@code MonotonicClock}.
   */
  public static MonotonicClock create(Clock clock) {
    return new MonotonicClock(clock, clock.now(), clock.nanoTime());
  }

  /**
   * Returns the current epoch timestamp in nanos calculated using {@link System#nanoTime()} since
   * the reference time read in the constructor.
   *
   * @return the current epoch timestamp in nanos.
   */
  @Override
  public long now() {
    long deltaNanos = clock.nanoTime() - this.nanoTime;
    return epochNanos + deltaNanos;
  }

  @Override
  public long nanoTime() {
    return clock.nanoTime();
  }
}
