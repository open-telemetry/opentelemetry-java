/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import io.opentelemetry.api.GlobalOpenTelemetry;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;

/**
 * This class provides a temporary global accessor for {@link ConfigProvider} until the
 * instrumentation config API is marked stable. It will eventually be merged into {@link
 * GlobalOpenTelemetry}.
 */
// We intentionally assign to be used for error reporting.
@SuppressWarnings("StaticAssignmentOfThrowable")
public final class GlobalConfigProvider {

  private static final AtomicReference<ConfigProvider> instance =
      new AtomicReference<>(ConfigProvider.noop());

  @SuppressWarnings("NonFinalStaticField")
  @Nullable
  private static volatile Throwable setInstanceCaller;

  private GlobalConfigProvider() {}

  /** Returns the globally registered {@link ConfigProvider}. */
  // instance cannot be set to null
  @SuppressWarnings("NullAway")
  public static ConfigProvider get() {
    return instance.get();
  }

  /**
   * Sets the global {@link ConfigProvider}. Future calls to {@link #get()} will return the provided
   * {@link ConfigProvider} instance. This should be called once as early as possible in your
   * application initialization logic.
   *
   * @throws IllegalStateException when called more than once
   */
  public static void set(ConfigProvider configProvider) {
    boolean changed = instance.compareAndSet(ConfigProvider.noop(), configProvider);
    if (!changed && (configProvider != ConfigProvider.noop())) {
      throw new IllegalStateException(
          "GlobalConfigProvider.set has already been called. GlobalConfigProvider.set "
              + "must be called only once before any calls to GlobalConfigProvider.get. "
              + "Previous invocation set to cause of this exception.",
          setInstanceCaller);
    }
    setInstanceCaller = new Throwable();
  }

  /**
   * Unsets the global {@link ConfigProvider}. This is only meant to be used from tests which need
   * to reconfigure {@link ConfigProvider}.
   */
  public static void resetForTest() {
    instance.set(ConfigProvider.noop());
  }
}
