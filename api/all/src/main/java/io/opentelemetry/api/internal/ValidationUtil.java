/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * General internal validation utility methods.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ValidationUtil {

  public static final String API_USAGE_LOGGER_NAME = "io.opentelemetry.ApiUsageLogging";

  private static final Logger API_USAGE_LOGGER = Logger.getLogger(API_USAGE_LOGGER_NAME);

  /**
   * Instrument names MUST conform to the following syntax.
   *
   * <ul>
   *   <li>They are not null or empty strings.
   *   <li>They are case-insensitive, ASCII strings.
   *   <li>The first character must be an alphabetic character.
   *   <li>Subsequent characters must belong to the alphanumeric characters, '_', '.', and '-'.
   *   <li>They can have a maximum length of 63 characters.
   * </ul>
   */
  private static final Pattern VALID_INSTRUMENT_NAME_PATTERN =
      Pattern.compile("([A-Za-z]){1}([A-Za-z0-9\\_\\-\\.]){0,62}");

  /**
   * Log the {@code message} to the {@link #API_USAGE_LOGGER_NAME API Usage Logger}.
   *
   * <p>Log at {@link Level#FINEST} and include a stack trace.
   */
  public static void log(String message) {
    log(message, Level.FINEST);
  }

  /**
   * Log the {@code message} to the {@link #API_USAGE_LOGGER_NAME API Usage Logger}.
   *
   * <p>Log includes a stack trace.
   */
  public static void log(String message, Level level) {
    API_USAGE_LOGGER.log(level, message, new AssertionError());
  }

  /** Determine if the instrument name is valid. If invalid, log a warning. */
  public static boolean isValidInstrumentName(String name) {
    return isValidInstrumentName(name, "");
  }

  /**
   * Determine if the instrument name is valid. If invalid, log a warning with the {@code logSuffix}
   * appended.
   */
  public static boolean isValidInstrumentName(String name, String logSuffix) {
    if (name == null || !VALID_INSTRUMENT_NAME_PATTERN.matcher(name).matches()) {
      API_USAGE_LOGGER.log(
          Level.WARNING,
          "Instrument name \""
              + name
              + "\" is invalid, returning noop instrument. Instrument names must consist of 63 or less characters including alphanumeric, _, ., -, and start with a letter."
              + logSuffix);
      return false;
    }
    return true;
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
    log(
        "Unit \""
            + unit
            + "\" is invalid. Instrument unit must be 63 or less ASCII characters."
            + logSuffix,
        Level.WARNING);
    return false;
  }

  private ValidationUtil() {}
}
