/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.trace.propagation;

import java.util.Objects;
import javax.annotation.concurrent.Immutable;

/**
 * Utilities for working with strings.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
final class StringUtils {

  /**
   * Returns {@code value} left-padded with zeroes to at least {@code minLength} characters.
   *
   * <p>If {@code value} is already at least {@code minLength} characters long, it is returned
   * unchanged.
   *
   * @param value the string to pad
   * @param minLength the minimum length of the returned string
   * @return the padded string, or {@code value} if it already meets the minimum length
   */
  static String padLeft(String value, int minLength) {
    return padStart(value, minLength, '0');
  }

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
  private static String padStart(String string, int minLength, char padChar) {
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

  private StringUtils() {}
}
