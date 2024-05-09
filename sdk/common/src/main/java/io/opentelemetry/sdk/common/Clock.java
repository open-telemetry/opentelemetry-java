/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import javax.annotation.concurrent.ThreadSafe;

/** Interface for getting the current time. */
@ThreadSafe
public interface Clock {

  /** Returns a default {@link Clock} which reads from {@linkplain System system time}. */
  static Clock getDefault() {
    return SystemClock.getInstance();
  }

  /**
   * Returns the current epoch timestamp in nanos from this clock. This timestamp should only be
   * used to compute a current time. To compute a duration, timestamps should always be obtained
   * using {@link #nanoTime()}. For example, this usage is correct.
   *
   * <pre>{@code
   * long startNanos = clock.nanoTime();
   * // Spend time...
   * long durationNanos = clock.nanoTime() - startNanos;
   * }</pre>
   *
   * <p>This usage is NOT correct.
   *
   * <pre>{@code
   * long startNanos = clock.now();
   * // Spend time...
   * long durationNanos = clock.now() - startNanos;
   * }</pre>
   *
   * <p>Calling this is equivalent to calling {@link #now(boolean)} with {@code highPrecision=true}.
   */
  long now();

  /**
   * Returns the current epoch timestamp in nanos from this clock.
   *
   * <p>This overload of {@link #now()} includes a {@code highPrecision} argument which specifies
   * whether the implementation should attempt to resolve higher precision at the potential expense
   * of performance. For example, in java 9+ its sometimes possible to resolve ns precision higher
   * than the ms precision of {@link System#currentTimeMillis()}, but doing so incurs a performance
   * penalty which some callers may wish to avoid. In contrast, we don't currently know if resolving
   * ns precision is possible in java 8, regardless of the value of {@code highPrecision}.
   *
   * <p>See {@link #now()} javadoc for details on usage.
   */
  default long now(boolean highPrecision) {
    return now();
  }

  /**
   * Returns a time measurement with nanosecond precision that can only be used to calculate elapsed
   * time.
   */
  long nanoTime();
}
