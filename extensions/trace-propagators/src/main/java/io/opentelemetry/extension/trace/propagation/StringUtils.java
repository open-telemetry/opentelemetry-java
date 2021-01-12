/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

// Includes work from:
/*
 * Copyright 2010, Guava Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

@Immutable
final class StringUtils {

  /**
   * Returns a string, of length at least {@code minLength}, consisting of {@code string} prepended
   * with as many copies of {@code padChar} as are necessary to reach that length. For example,
   *
   * <ul>
   *   <li>{@code padStart("7", 3, '0')} returns {@code "007"}
   *   <li>{@code padStart("2010", 3, '0')} returns {@code "2010"}
   * </ul>
   *
   * <p>See {@link java.util.Formatter} for a richer set of formatting capabilities.
   *
   * <p>This method was copied almost verbatim from Guava library method
   * com.google.common.base.Strings#padStart(java.lang.String, int, char).
   *
   * @param string the string which should appear at the end of the result
   * @param minLength the minimum length the resulting string must have. Can be zero or negative, in
   *     which case the input string is always returned.
   * @param padChar the character to insert at the beginning of the result until the minimum length
   *     is reached
   * @return the padded string
   */
  static String padStart(String string, int minLength, char padChar) {
    Objects.requireNonNull(string);
    if (string.length() >= minLength) {
      return string;
    }
    StringBuilder sb = new StringBuilder(minLength);
    for (int i = string.length(); i < minLength; i++) {
      sb.append(padChar);
    }
    sb.append(string);
    return sb.toString();
  }

  /**
   * Returns {@code true} if the given string is null or is the empty string.
   *
   * <p>This method was copied verbatim from Guava library method
   * com.google.common.base.Strings#isNullOrEmpty(java.lang.String).
   *
   * @param string a string reference to check
   * @return {@code true} if the string is null or is the empty string
   */
  static boolean isNullOrEmpty(String string) {
    return string == null || string.isEmpty();
  }

  /**
   * Pads a given string on the left with leading 0's up the length.
   *
   * @param value the string to pad
   * @param minLength the minimum length the resulting padded string must have. Can be zero or
   *     negative, in which case the input string is always returned.
   * @return the padded string
   */
  static String padLeft(String value, int minLength) {
    return padStart(value, minLength, '0');
  }

  private StringUtils() {}
}
