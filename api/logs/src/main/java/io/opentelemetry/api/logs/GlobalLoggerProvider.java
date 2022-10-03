/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.GlobalOpenTelemetry;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * This class provides a temporary global accessor for {@link LoggerProvider} until the log API is
 * marked stable. It will eventually be merged into {@link GlobalOpenTelemetry}.
 */
// We intentionally assign to be used for error reporting.
@SuppressWarnings("StaticAssignmentOfThrowable")
public final class GlobalLoggerProvider {

  private static final AtomicReference<LoggerProvider> instance =
      new AtomicReference<>(LoggerProvider.noop());

  @Nullable private static volatile Throwable setInstanceCaller;

  private GlobalLoggerProvider() {}

  /** Returns the globally registered {@link LoggerProvider}. */
  // instance cannot be set to null
  @SuppressWarnings("NullAway")
  public static LoggerProvider get() {
    return instance.get();
  }

  /**
   * Sets the global {@link LoggerProvider}. Future calls to {@link #get()} will return the provided
   * {@link LoggerProvider} instance. This should be called once as early as possible in your
   * application initialization logic.
   */
  public static void set(LoggerProvider loggerProvider) {
    boolean changed = instance.compareAndSet(LoggerProvider.noop(), loggerProvider);
    if (!changed && (loggerProvider != LoggerProvider.noop())) {
      throw new IllegalStateException(
          "GlobalLoggerProvider.set has already been called. GlobalLoggerProvider.set "
              + "must be called only once before any calls to GlobalLoggerProvider.get. "
              + "Previous invocation set to cause of this exception.",
          setInstanceCaller);
    }
    setInstanceCaller = new Throwable();
  }

  /**
   * Unsets the global {@link LoggerProvider}. This is only meant to be used from tests which need
   * to reconfigure {@link LoggerProvider}.
   */
  public static void resetForTest() {
    instance.set(LoggerProvider.noop());
  }
}
