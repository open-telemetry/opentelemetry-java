/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.GlobalOpenTelemetry;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * This class provides a temporary global accessor for {@link EventEmitterProvider} until the event
 * API is marked stable. It will eventually be merged into {@link GlobalOpenTelemetry}.
 */
// We intentionally assign to be used for error reporting.
@SuppressWarnings("StaticAssignmentOfThrowable")
public final class GlobalEventEmitterProvider {

  private static final AtomicReference<EventEmitterProvider> instance =
      new AtomicReference<>(EventEmitterProvider.noop());

  @Nullable private static volatile Throwable setInstanceCaller;

  private GlobalEventEmitterProvider() {}

  /** Returns the globally registered {@link EventEmitterProvider}. */
  // instance cannot be set to null
  @SuppressWarnings("NullAway")
  public static EventEmitterProvider get() {
    return instance.get();
  }

  /**
   * Sets the global {@link EventEmitterProvider}. Future calls to {@link #get()} will return the
   * provided {@link EventEmitterProvider} instance. This should be called once as early as possible
   * in your application initialization logic.
   */
  public static void set(EventEmitterProvider eventEmitterProvider) {
    boolean changed = instance.compareAndSet(EventEmitterProvider.noop(), eventEmitterProvider);
    if (!changed && (eventEmitterProvider != EventEmitterProvider.noop())) {
      throw new IllegalStateException(
          "GlobalEventEmitterProvider.set has already been called. GlobalEventEmitterProvider.set "
              + "must be called only once before any calls to GlobalEventEmitterProvider.get. "
              + "Previous invocation set to cause of this exception.",
          setInstanceCaller);
    }
    setInstanceCaller = new Throwable();
  }

  /**
   * Unsets the global {@link EventEmitterProvider}. This is only meant to be used from tests which
   * need to reconfigure {@link EventEmitterProvider}.
   */
  public static void resetForTest() {
    instance.set(EventEmitterProvider.noop());
  }
}
