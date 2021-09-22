/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.exporter.otlp.internal.retry;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public class RetryContext {

  private final AtomicInteger attemptCount = new AtomicInteger();
  private final AtomicBoolean attemptStarted = new AtomicBoolean();
  private final AtomicReference<Throwable> lastAttemptFailure = new AtomicReference<>();

  RetryContext() {}

  /** Return the number of completed attempts. */
  public int getAttemptCount() {
    return attemptCount.get();
  }

  /**
   * Return the error of the last completed attempt, or null if the last attempt completed
   * successfully.
   */
  public Throwable getLastAttemptFailure() {
    return lastAttemptFailure.get();
  }

  void startAttempt() {
    if (!attemptStarted.compareAndSet(false, true)) {
      throw new IllegalStateException("Cannot start before active attempt is complete.");
    }
  }

  void complete() {
    complete(null);
  }

  void complete(@Nullable Throwable t) {
    if (attemptStarted.compareAndSet(true, false)) {
      attemptCount.incrementAndGet();
      lastAttemptFailure.set(t);
    } else {
      throw new IllegalStateException("Cannot complete before attempt is started.");
    }
  }
}
