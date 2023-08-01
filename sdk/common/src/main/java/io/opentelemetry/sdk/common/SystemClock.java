/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common;

import io.opentelemetry.sdk.internal.JavaVersionSpecific;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Clock} that uses {@link JavaVersionSpecific#currentTimeNanos()} and {@link
 * System#nanoTime()}.
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
    return JavaVersionSpecific.get().currentTimeNanos();
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
