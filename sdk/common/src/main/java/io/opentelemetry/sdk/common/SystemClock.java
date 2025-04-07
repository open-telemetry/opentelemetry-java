/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Clock} that uses {@link java.time.Clock#systemUTC()}, {@link
 * System#currentTimeMillis()}, and {@link System#nanoTime()}.
 */
@ThreadSafe
final class SystemClock implements Clock {

  private static final SystemClock INSTANCE = new SystemClock();

  private SystemClock() {}

  /** Returns a {@link SystemClock}. */
  static Clock getInstance() {
    return INSTANCE;
  }

  @Override
  public long now() {
    return now(true);
  }

  @Override
  public long now(boolean highPrecision) {
    if (highPrecision) {
      Instant now = java.time.Clock.systemUTC().instant();
      return TimeUnit.SECONDS.toNanos(now.getEpochSecond()) + now.getNano();
    }
    return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
  }

  @Override
  public long nanoTime() {
    return System.nanoTime();
  }

  @Override
  public String toString() {
    return "SystemClock{}";
  }
}
