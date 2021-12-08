/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

/**
 * Interface mirroring the {@link java.util.concurrent.atomic.DoubleAdder} API, with implementation
 * that varies based on availability of {@link java.util.concurrent.atomic.DoubleAdder}. This offers
 * compatibility for Android 21 without compromising performance in runtimes where {@link
 * java.util.concurrent.atomic.DoubleAdder} is available.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see AdderUtil#createDoubleAdder()
 */
public interface DoubleAdder {

  /**
   * Add the given value.
   *
   * @param x the value to add
   * @see java.util.concurrent.atomic.DoubleAdder#add(double)
   */
  void add(double x);

  /**
   * Returns the current sum.
   *
   * @see java.util.concurrent.atomic.DoubleAdder#sum()
   */
  double sum();

  /**
   * Resets the variables maintaining the sum to zero.
   *
   * @see java.util.concurrent.atomic.DoubleAdder#reset()
   */
  void reset();

  /**
   * Equivalent in effect to {@link #sum()} followed by {@link #reset()}.
   *
   * @return the sum
   * @see java.util.concurrent.atomic.DoubleAdder#sumThenReset()
   */
  double sumThenReset();

  /**
   * Returns the {@link #sum} as a {@code long} after a narrowing primitive conversion.
   *
   * @see java.util.concurrent.atomic.DoubleAdder#toString()
   */
  default long longValue() {
    return (long) sum();
  }

  /**
   * Returns the {@link #sum} as a {@code int} after a narrowing primitive conversion.
   *
   * @see java.util.concurrent.atomic.DoubleAdder#toString()
   */
  default int intValue() {
    return (int) sum();
  }

  /**
   * Returns the {@link #sum} as a {@code float} after a narrowing primitive conversion.
   *
   * @see java.util.concurrent.atomic.DoubleAdder#toString()
   */
  default float floatValue() {
    return (float) sum();
  }

  /**
   * Equivalent to {@link #sum()}.
   *
   * @return the sum
   * @see java.util.concurrent.atomic.DoubleAdder#toString()
   */
  default double doubleValue() {
    return sum();
  }
}
