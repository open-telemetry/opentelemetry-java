/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A utility for logging API misuse, allowing operators to diagnose invalid usage with a single
 * logging configuration entry.
 *
 * <p>Logs at {@link Level#FINEST} by default, so messages are silent in production unless
 * explicitly enabled. Each log record includes a {@link Throwable} to make the offending call site
 * visible in the stack trace without requiring the exception to be thrown.
 *
 * <p>To investigate API misuse, configure the logger named {@code io.opentelemetry.usage} at {@link
 * Level#FINEST} in development, or periodically in staging/production.
 *
 * <p>This class is public for use by OpenTelemetry component authors. It is not intended for use by
 * application developers.
 */
public final class ApiUsageLogger {

  /** The logger name used for all API-misuse diagnostics. */
  private static final Logger LOGGER = Logger.getLogger("io.opentelemetry.usage");

  /**
   * Log a misuse of {@code apiClass#methodName} with the given {@code message}.
   *
   * <p>Logs at {@link Level#FINEST} and includes a stack trace.
   *
   * @param apiClass the public API class where the misuse occurred
   * @param methodName the name of the method where the misuse occurred
   * @param message a brief description of the problem
   */
  public static void log(Class<?> apiClass, String methodName, String message) {
    log(apiClass, methodName, message, Level.FINEST);
  }

  // Visible for testing
  static void log(Class<?> apiClass, String methodName, String message, Level level) {
    if (LOGGER.isLoggable(level)) {
      LOGGER.log(
          level, apiClass.getSimpleName() + "." + methodName + "(): " + message, new Throwable());
    }
  }

  private ApiUsageLogger() {}
}
