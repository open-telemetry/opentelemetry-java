/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/** General internal validation utility methods. */
@Immutable
public final class ValidationUtil {

  private ValidationUtil() {}

  private static final Logger API_USAGE_LOGGER =
      Logger.getLogger("io.opentelemetry.ApiUsageLogging");

  public static void log(String msg, Throwable thrown) {
    API_USAGE_LOGGER.log(Level.FINEST, msg, thrown);
  }
}
