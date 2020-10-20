/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.Clock;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/** A {@link Clock} that uses {@link System#currentTimeMillis()} and {@link System#nanoTime()}. */
@ThreadSafe
public final class MillisClock implements Clock {

  private static final MillisClock INSTANCE = new MillisClock();

  private MillisClock() {}

  /**
   * Returns a {@code MillisClock}.
   *
   * @return a {@code MillisClock}.
   */
  public static MillisClock getInstance() {
    return INSTANCE;
  }

  @Override
  public long now() {
    return TimeUnit.MILLISECONDS.toNanos(System.currentTimeMillis());
  }

  @Override
  public long nanoTime() {
    return System.nanoTime();
  }
}
