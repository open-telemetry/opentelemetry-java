/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Helper for API misuse logging.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ApiUsageLogger {

  private static final Logger API_USAGE_LOGGER = Logger.getLogger(ApiUsageLogger.class.getName());

  /**
   * Log the {@code message} to the {@link #API_USAGE_LOGGER API Usage Logger}.
   *
   * <p>Log at {@link Level#FINEST} and include a stack trace.
   */
  public static void log(String message) {
    log(message, Level.FINEST);
  }

  /**
   * Log the {@code message} to the {@link #API_USAGE_LOGGER API Usage Logger}.
   *
   * <p>Log includes a stack trace.
   */
  public static void log(String message, Level level) {
    if (API_USAGE_LOGGER.isLoggable(level)) {
      API_USAGE_LOGGER.log(level, message, new AssertionError());
    }
  }

  private ApiUsageLogger() {}
}
