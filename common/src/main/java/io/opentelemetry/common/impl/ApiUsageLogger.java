/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common.impl;

import java.util.concurrent.atomic.AtomicBoolean;
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
 * <p>The first time any API misuse is detected, a one-time {@link Level#WARNING} is emitted to the
 * {@code io.opentelemetry.usage} logger. This warning is visible under default logging
 * configuration and signals that API misuse is occurring. To see the full details of every misuse
 * event (message and stack trace), configure the {@code io.opentelemetry.usage} logger at {@link
 * Level#FINEST}.
 *
 * <p>This class is not intended for use by application developers. Its API is stable and will not
 * be changed or removed in a backwards-incompatible manner.
 *
 * @since 1.62.0
 */
public final class ApiUsageLogger {

  /** The logger name used for all API-misuse diagnostics. */
  private static final Logger LOGGER = Logger.getLogger("io.opentelemetry.usage");

  private static final AtomicBoolean WARN_ONCE = new AtomicBoolean();

  /**
   * Log that {@code paramName} was null in {@code apiClass#methodName}.
   *
   * <p>Convenience overload of {@link #logUsageIssue(Class, String, String)} for the common case of
   * a null parameter that should not be null.
   *
   * @param apiClass the public API class where the misuse occurred
   * @param methodName the name of the method where the misuse occurred
   * @param paramName the name of the parameter that was null
   */
  public static void logNullParam(Class<?> apiClass, String methodName, String paramName) {
    logUsageIssue(apiClass, methodName, paramName + " is null");
  }

  /**
   * Log a misuse of {@code apiClass#methodName} with the given {@code message}.
   *
   * <p>Logs at {@link Level#FINEST} and includes a stack trace.
   *
   * @param apiClass the public API class where the misuse occurred
   * @param methodName the name of the method where the misuse occurred
   * @param message a brief description of the problem
   */
  public static void logUsageIssue(Class<?> apiClass, String methodName, String message) {
    if (WARN_ONCE.compareAndSet(false, true)) {
      LOGGER.warning(
          "OpenTelemetry API usage issue detected. To see more details, enable FINEST logging for io.opentelemetry.usage. Stacktraces are includes to identify the offending call site.");
    }
    if (LOGGER.isLoggable(Level.FINEST)) {
      LOGGER.log(
          Level.FINEST,
          apiClass.getSimpleName() + "." + methodName + "(): " + message,
          new Throwable());
    }
  }

  private ApiUsageLogger() {}
}
