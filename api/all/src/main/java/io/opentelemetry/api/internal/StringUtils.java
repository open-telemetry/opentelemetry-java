/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * Utilities for working with strings.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
@Immutable
public final class StringUtils {

  /**
   * Returns {@code true} if the given string is null or is the empty string.
   *
   * <p>This method was copied verbatim from Guava library method
   * com.google.common.base.Strings#isNullOrEmpty(java.lang.String).
   *
   * @param string a string reference to check
   * @return {@code true} if the string is null or is the empty string
   */
  @Contract("null -> true")
  public static boolean isNullOrEmpty(@Nullable String string) {
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
  public static String padLeft(String value, int minLength) {
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

  /**
   * Determines whether the {@code String} contains only printable characters.
   *
   * @param str the {@code String} to be validated.
   * @return whether the {@code String} contains only printable characters.
   */
  public static boolean isPrintableString(String str) {
    if (isNullOrEmpty(str)) {
      return false;
    }
    for (int i = 0; i < str.length(); i++) {
      if (!isPrintableChar(str.charAt(i))) {
        return false;
      }
    }
    return true;
  }

  private static boolean isPrintableChar(char ch) {
    return ch >= ' ' && ch <= '~';
  }

  private StringUtils() {}
}
