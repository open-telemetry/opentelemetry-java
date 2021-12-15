/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

/** This class is a temporary solution until log SDK is marked stable. */
public class GlobalLogEmitterProvider {
  private static volatile LogEmitterProvider globalLogEmitterProvider = LogEmitterProvider.noop();

  private GlobalLogEmitterProvider() {}

  /** Returns the globally registered {@link LogEmitterProvider}. */
  public static LogEmitterProvider get() {
    return globalLogEmitterProvider;
  }

  /**
   * Sets the {@link LogEmitterProvider} that should be the global instance. Future calls to {@link
   * #get()} will return the provided {@link LogEmitterProvider} instance. This should be called
   * once as early as possible in your application initialization logic, often in a {@code static}
   * block in your main class.
   */
  public static void set(LogEmitterProvider provider) {
    globalLogEmitterProvider = (provider == null) ? LogEmitterProvider.noop() : provider;
  }
}
