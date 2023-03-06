/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.metrics.internal;

import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;

/**
 * Utility for validating instrument names. This class is internal to the SDK and is not intended
 * for public use.
 */
public class InstrumentNameValidator {

  public static final String LOGGER_NAME =
      "io.opentelemetry.sdk.metrics.internal.InstrumentNameValidator";
  private static final Logger LOGGER = Logger.getLogger(LOGGER_NAME);

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

  /** Check if the instrument name is valid. If invalid, log a warning. */
  public static boolean checkValidInstrumentName(String name) {
    return checkValidInstrumentName(name, "");
  }

  /**
   * Check if the instrument name is valid. If invalid, log a warning with the {@code logSuffix}
   * appended.
   */
  public static boolean checkValidInstrumentName(String name, String logSuffix) {
    if (name != null && VALID_INSTRUMENT_NAME_PATTERN.matcher(name).matches()) {
      return true;
    }
    if (LOGGER.isLoggable(Level.WARNING)) {
      LOGGER.log(
          Level.WARNING,
          "Instrument name \""
              + name
              + "\" is invalid, returning noop instrument. Instrument names must consist of 63 or fewer characters including alphanumeric, _, ., -, and start with a letter."
              + logSuffix,
          new AssertionError());
    }

    return false;
  }

  private InstrumentNameValidator() {}
}
