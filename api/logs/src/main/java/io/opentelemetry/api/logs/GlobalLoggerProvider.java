/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.GlobalOpenTelemetry;

/**
 * This class provides a temporary global accessor for {@link LoggerProvider} until the log API is
 * marked stable. It will eventually be merged into {@link GlobalOpenTelemetry}.
 */
public final class GlobalLoggerProvider {

  private static volatile LoggerProvider globalLoggerProvider = DefaultLoggerProvider.getInstance();

  private GlobalLoggerProvider() {}

  /** Returns the globally registered {@link LoggerProvider}. */
  public static LoggerProvider get() {
    return globalLoggerProvider;
  }

  /**
   * Sets the global {@link LoggerProvider}. Future calls to {@link #get()} will return the provided
   * {@link LoggerProvider} instance. This should be called once as early as possible in your
   * application initialization logic.
   */
  public static void set(LoggerProvider loggerProvider) {
    globalLoggerProvider =
        loggerProvider == null ? DefaultLoggerProvider.getInstance() : loggerProvider;
  }
}
