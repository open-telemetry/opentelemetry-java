/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import io.opentelemetry.api.GlobalOpenTelemetry;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * This class provides a temporary global accessor for {@link EventLoggerProvider} until the event
 * API is marked stable. It will eventually be merged into {@link GlobalOpenTelemetry}.
 */
// We intentionally assign to be used for error reporting.
@SuppressWarnings("StaticAssignmentOfThrowable")
public final class GlobalEventLoggerProvider {

  private static final AtomicReference<EventLoggerProvider> instance =
      new AtomicReference<>(EventLoggerProvider.noop());

  @SuppressWarnings("NonFinalStaticField")
  @Nullable
  private static volatile Throwable setInstanceCaller;

  private GlobalEventLoggerProvider() {}

  /** Returns the globally registered {@link EventLoggerProvider}. */
  // instance cannot be set to null
  @SuppressWarnings("NullAway")
  public static EventLoggerProvider get() {
    return instance.get();
  }

  /**
   * Sets the global {@link EventLoggerProvider}. Future calls to {@link #get()} will return the
   * provided {@link EventLoggerProvider} instance. This should be called once as early as possible
   * in your application initialization logic.
   */
  public static void set(EventLoggerProvider eventLoggerProvider) {
    boolean changed = instance.compareAndSet(EventLoggerProvider.noop(), eventLoggerProvider);
    if (!changed && (eventLoggerProvider != EventLoggerProvider.noop())) {
      throw new IllegalStateException(
          "GlobalEventLoggerProvider.set has already been called. GlobalEventLoggerProvider.set "
              + "must be called only once before any calls to GlobalEventLoggerProvider.get. "
              + "Previous invocation set to cause of this exception.",
          setInstanceCaller);
    }
    setInstanceCaller = new Throwable();
  }

  /**
   * Unsets the global {@link EventLoggerProvider}. This is only meant to be used from tests which
   * need to reconfigure {@link EventLoggerProvider}.
   */
  public static void resetForTest() {
    instance.set(EventLoggerProvider.noop());
  }
}
