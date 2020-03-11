/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.internal;

import javax.annotation.concurrent.Immutable;

/** Internal utility methods for working with attribute keys, attribute values, and metric names. */
@Immutable
public final class StringUtils {

  public static final int NAME_MAX_LENGTH = 255;

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

  public static boolean isNullOrEmpty(String value) {
    return value == null || value.length() == 0;
  }

  /**
   * Determines whether the metric name contains a valid metric name.
   *
   * @param metricName the metric name to be validated.
   * @return whether the metricName contains a valid name.
   */
  public static boolean isValidMetricName(String metricName) {
    if (metricName.isEmpty() || metricName.length() > NAME_MAX_LENGTH) {
      return false;
    }
    String pattern = "[aA-zZ][aA-zZ0-9_\\-.]*";
    return metricName.matches(pattern);
  }

  /**
   * Pads a given string on the left with leading 0's up the length.
   *
   * @param value the string to pad
   * @param length the number if characters to pad up to
   * @return the padded string
   */
  public static String padLeft(String value, int length) {
    if (value == null || length <= 0) {
      throw new IllegalArgumentException();
    }

    if (value.length() >= length) {
      return value;
    }
    return String.format("%1$" + length + "s", value).replace(' ', '0');
  }

  private StringUtils() {}
}
