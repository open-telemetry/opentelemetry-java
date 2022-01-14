/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal.concurrent;

/**
 * Interface mirroring the {@link java.util.concurrent.atomic.LongAdder} API, with implementation
 * that varies based on availability of {@link java.util.concurrent.atomic.LongAdder}. This offers
 * compatibility for Android 21 without compromising performance in runtimes where {@link
 * java.util.concurrent.atomic.LongAdder} is available.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see AdderUtil#createLongAdder()
 */
public interface LongAdder {

  /**
   * Equivalent to {@code add(1)}.
   *
   * @see java.util.concurrent.atomic.LongAdder#add(long)
   */
  default void increment() {
    add(1L);
  }

  /**
   * Equivalent to {@code add(-1)}.
   *
   * @see java.util.concurrent.atomic.LongAdder#add(long)
   */
  default void decrement() {
    add(-1L);
  }

  /**
   * Add the given value.
   *
   * @param x the value to add
   * @see java.util.concurrent.atomic.LongAdder#add(long)
   */
  void add(long x);

  /**
   * Returns the current sum.
   *
   * @see java.util.concurrent.atomic.LongAdder#sum()
   */
  long sum();

  /**
   * Resets the variables maintaining the sum to zero.
   *
   * @see java.util.concurrent.atomic.LongAdder#reset()
   */
  void reset();

  /**
   * Equivalent in effect to {@link #sum()} followed by {@link #reset()}.
   *
   * @return the sum
   * @see java.util.concurrent.atomic.LongAdder#sumThenReset()
   */
  long sumThenReset();

  /**
   * Equivalent to {@link #sum()}.
   *
   * @return the sum
   * @see java.util.concurrent.atomic.LongAdder#toString()
   */
  default long longValue() {
    return sum();
  }

  /**
   * Returns the {@link #sum} as a {@code int} after a narrowing primitive conversion.
   *
   * @see java.util.concurrent.atomic.LongAdder#toString()
   */
  default int intValue() {
    return (int) sum();
  }

  /**
   * Returns the {@link #sum} as a {@code float} after a widening primitive conversion.
   *
   * @see java.util.concurrent.atomic.LongAdder#toString()
   */
  default float floatValue() {
    return (float) sum();
  }

  /**
   * Returns the {@link #sum} as a {@code double} after a widening primitive conversion.
   *
   * @see java.util.concurrent.atomic.LongAdder#doubleValue() ()
   */
  default double doubleValue() {
    return (double) sum();
  }
}
