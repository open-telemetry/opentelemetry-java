/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.sdk.common.Clock;
import javax.annotation.concurrent.Immutable;

/**
 * A utility for returning wall times anchored to a given point in time. Wall time measurements will
 * not be taken from the system, but instead are computed by adding {@linkplain System#nanoTime()
 * monotonic time} to the anchor point.
 *
 * <p>This is needed because Java has lower granularity for epoch times and tracing events are
 * recorded more often. There is also a performance improvement in avoiding referencing the system's
 * wall time where possible. Instead of computing a true wall time for every timestamp within a
 * trace, we compute it once at the local root and then anchor all descendant span timestamps to
 * this root's timestamp.
 */
@Immutable
final class AnchoredClock {
  private final Clock clock;
  private final long epochNanos;
  private final long nanoTime;

  private AnchoredClock(Clock clock, long epochNanos, long nanoTime) {
    this.clock = clock;
    this.epochNanos = epochNanos;
    this.nanoTime = nanoTime;
  }

  /**
   * Returns a {@code AnchoredClock}.
   *
   * @param clock the {@code Clock} to be used to read the current epoch time and nanoTime.
   * @return a {@code MonotonicClock}.
   */
  public static AnchoredClock create(Clock clock) {
    return new AnchoredClock(clock, clock.now(), clock.nanoTime());
  }

  /**
   * Returns the current epoch timestamp in nanos calculated using {@link System#nanoTime()} since
   * the reference time read in the constructor. This time can be used for computing durations.
   *
   * @return the current epoch timestamp in nanos.
   */
  long now() {
    long deltaNanos = clock.nanoTime() - this.nanoTime;
    return epochNanos + deltaNanos;
  }

  /** Returns the start time in nanos of this {@link AnchoredClock}. */
  long startTime() {
    return epochNanos;
  }
}
