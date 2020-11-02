/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.sdk.common.Clock;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.ThreadSafe;

/** A {@link Clock} that uses {@link System#currentTimeMillis()} and {@link System#nanoTime()}. */
@ThreadSafe
public final class InstantClock implements Clock {

  private static final InstantClock INSTANCE = new InstantClock();

  private InstantClock() {}

  /**
   * Returns a {@code MillisClock}.
   *
   * @return a {@code MillisClock}.
   */
  public static InstantClock getInstance() {
    return INSTANCE;
  }

  @Override
  public long now() {
    Instant now = Instant.now();
    return TimeUnit.SECONDS.toNanos(now.getEpochSecond()) + now.getNano();
  }

  @Override
  public long nanoTime() {
    return System.nanoTime();
  }
}
