/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.zpages;

import java.util.concurrent.TimeUnit;

/**
 * A class of boundaries for the latency buckets. The completed spans with a status of {@link
 * io.opentelemetry.api.trace.Status#OK} are categorized into one of these buckets om the traceZ
 * zPage.
 */
enum LatencyBoundary {
  /** Stores finished successful requests of duration within the interval [0, 10us). */
  ZERO_MICROSx10(0, TimeUnit.MICROSECONDS.toNanos(10)),

  /** Stores finished successful requests of duration within the interval [10us, 100us). */
  MICROSx10_MICROSx100(TimeUnit.MICROSECONDS.toNanos(10), TimeUnit.MICROSECONDS.toNanos(100)),

  /** Stores finished successful requests of duration within the interval [100us, 1ms). */
  MICROSx100_MILLIx1(TimeUnit.MICROSECONDS.toNanos(100), TimeUnit.MILLISECONDS.toNanos(1)),

  /** Stores finished successful requests of duration within the interval [1ms, 10ms). */
  MILLIx1_MILLIx10(TimeUnit.MILLISECONDS.toNanos(1), TimeUnit.MILLISECONDS.toNanos(10)),

  /** Stores finished successful requests of duration within the interval [10ms, 100ms). */
  MILLIx10_MILLIx100(TimeUnit.MILLISECONDS.toNanos(10), TimeUnit.MILLISECONDS.toNanos(100)),

  /** Stores finished successful requests of duration within the interval [100ms, 1sec). */
  MILLIx100_SECONDx1(TimeUnit.MILLISECONDS.toNanos(100), TimeUnit.SECONDS.toNanos(1)),

  /** Stores finished successful requests of duration within the interval [1sec, 10sec). */
  SECONDx1_SECONDx10(TimeUnit.SECONDS.toNanos(1), TimeUnit.SECONDS.toNanos(10)),

  /** Stores finished successful requests of duration within the interval [10sec, 100sec). */
  SECONDx10_SECONDx100(TimeUnit.SECONDS.toNanos(10), TimeUnit.SECONDS.toNanos(100)),

  /** Stores finished successful requests of duration greater than or equal to 100sec. */
  SECONDx100_MAX(TimeUnit.SECONDS.toNanos(100), Long.MAX_VALUE);

  private final long latencyLowerBound;
  private final long latencyUpperBound;

  /**
   * Constructs a {@code LatencyBoundaries} with the given boundaries and label.
   *
   * @param latencyLowerBound the latency lower bound of the bucket.
   * @param latencyUpperBound the latency upper bound of the bucket.
   */
  LatencyBoundary(long latencyLowerBound, long latencyUpperBound) {
    this.latencyLowerBound = latencyLowerBound;
    this.latencyUpperBound = latencyUpperBound;
  }

  /**
   * Returns the latency lower bound of the bucket.
   *
   * @return the latency lower bound of the bucket.
   */
  long getLatencyLowerBound() {
    return latencyLowerBound;
  }

  /**
   * Returns the latency upper bound of the bucket.
   *
   * @return the latency upper bound of the bucket.
   */
  long getLatencyUpperBound() {
    return latencyUpperBound;
  }

  /**
   * Returns the LatencyBoundary that the argument falls into.
   *
   * @param latencyNanos latency in nanoseconds.
   * @return the LatencyBoundary that latencyNanos falls into.
   */
  static LatencyBoundary getBoundary(long latencyNanos) {
    for (LatencyBoundary bucket : LatencyBoundary.values()) {
      if (latencyNanos >= bucket.getLatencyLowerBound()
          && latencyNanos < bucket.getLatencyUpperBound()) {
        return bucket;
      }
    }
    return ZERO_MICROSx10;
  }
}
