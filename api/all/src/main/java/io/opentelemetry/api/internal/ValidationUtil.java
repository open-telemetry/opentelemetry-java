/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.concurrent.Immutable;

/**
 * General internal validation utility methods.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class ValidationUtil {

  public static final String API_USAGE_LOGGER_NAME = "io.opentelemetry.ApiUsageLogging";

  private static final Logger API_USAGE_LOGGER = Logger.getLogger(API_USAGE_LOGGER_NAME);

  public static void log(String msg) {
    API_USAGE_LOGGER.log(Level.FINEST, msg, new AssertionError());
  }

  /** Determine if the instrument unit is valid. If invalid, log a warning. */
  public static boolean isValidInstrumentUnit(String unit) {
    return isValidInstrumentUnit(unit, "");
  }

  /**
   * Determine if the instrument unit is valid. If invalid, log a warning with the {@code logSuffix}
   * appended.
   */
  public static boolean isValidInstrumentUnit(String unit, String logSuffix) {
    if (unit != null
        && !unit.equals("")
        && unit.length() < 64
        && StandardCharsets.US_ASCII.newEncoder().canEncode(unit)) {
      return true;
    }
    API_USAGE_LOGGER.log(
        Level.WARNING,
        "Unit \""
            + unit
            + "\" is invalid. Instrument unit must be 63 or less ASCII characters."
            + logSuffix);
    return false;
  }

  private ValidationUtil() {}
}
