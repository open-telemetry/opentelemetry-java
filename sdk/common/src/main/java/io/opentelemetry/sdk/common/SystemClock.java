/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.sdk.internal.JavaVersionSpecific;
import javax.annotation.concurrent.ThreadSafe;

/** A {@link Clock} that uses {@link System#currentTimeMillis()} and {@link System#nanoTime()}. */
@ThreadSafe
final class SystemClock implements Clock {

  private static final SystemClock INSTANCE = new SystemClock();

  private SystemClock() {}

  /**
   * Returns a {@code MillisClock}.
   *
   * @return a {@code MillisClock}.
   */
  static Clock getInstance() {
    return INSTANCE;
  }

  @Override
  public long now() {
    return JavaVersionSpecific.get().currentTimeNanos();
  }

  @Override
  public long nanoTime() {
    return System.nanoTime();
  }
}
