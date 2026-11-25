/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

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
   * Determines whether the {@code String} contains only printable characters.
   *
   * @param str the {@code String} to be validated.
   * @return whether the {@code String} contains only printable characters.
   */
  public static boolean isPrintableString(String str) {
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
