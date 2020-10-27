/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import javax.annotation.concurrent.Immutable;

/** Internal utility methods for working with attribute keys, attribute values, and metric names. */
@Immutable
public final class StringUtils {

  public static final int METRIC_NAME_MAX_LENGTH = 255;

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

  /**
   * Determines whether the metric name contains a valid metric name.
   *
   * @param metricName the metric name to be validated.
   * @return whether the metricName contains a valid name.
   */
  public static boolean isValidMetricName(String metricName) {
    if (metricName.isEmpty() || metricName.length() > METRIC_NAME_MAX_LENGTH) {
      return false;
    }
    String pattern = "[aA-zZ][aA-zZ0-9_\\-.]*";
    return metricName.matches(pattern);
  }

  private StringUtils() {}
}
