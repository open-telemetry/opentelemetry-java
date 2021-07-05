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
   *
   * long startNanos = clock.nanoTime();
   * // Spend time...
   * long durationNanos = clock.nanoTime() - startNanos;
   *
   * }</pre>
   *
   * <p>This usage is not correct.
   *
   * <pre>{@code
   * long startNanos = clock.now();
   * // Spend time...
   * long durationNanos = clock.now() - startNanos;
   *
   * }</pre>
   */
  long now();

  /**
   * Returns a time measurement with nanosecond precision that can only be used to calculate elapsed
   * time.
   */
  long nanoTime();
}
