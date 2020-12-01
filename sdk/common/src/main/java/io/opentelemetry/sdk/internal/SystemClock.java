/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.Clock;
import javax.annotation.concurrent.ThreadSafe;

/** A {@link Clock} that uses {@link System#currentTimeMillis()} and {@link System#nanoTime()}. */
@ThreadSafe
public final class SystemClock implements Clock {

  private static final SystemClock INSTANCE = new SystemClock();

  private SystemClock() {}

  /**
   * Returns a {@code MillisClock}.
   *
   * @return a {@code MillisClock}.
   */
  public static SystemClock getInstance() {
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
