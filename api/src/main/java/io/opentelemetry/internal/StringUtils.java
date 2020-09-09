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

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.AttributeKeyImpl;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;
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

  /**
   * If given attribute is of type STRING and has more characters than given {@code limit} then
   * return new AttributeValue with string truncated to {@code limit} characters.
   *
   * <p>If given attribute is of type STRING_ARRAY and non-empty then return new AttributeValue with
   * every element truncated to {@code limit} characters.
   *
   * <p>Otherwise return given {@code value}
   *
   * @throws IllegalArgumentException if limit is zero or negative
   */
  @SuppressWarnings("unchecked")
  public static <T> T truncateToSize(AttributeKey<T> key, T value, int limit) {
    Utils.checkArgument(limit > 0, "attribute value limit must be positive, got %d", limit);

    if (value == null
        || (!(key instanceof AttributeKeyImpl.StringKey)
            && !(key instanceof AttributeKeyImpl.StringArrayKey))) {
      return value;
    }

    if (key instanceof AttributeKeyImpl.StringArrayKey) {
      List<String> strings = (List<String>) value;
      if (strings.isEmpty()) {
        return value;
      }

      List<String> newStrings = new ArrayList<>(strings.size());
      for (String string : strings) {
        newStrings.add(truncateToSize(string, limit));
      }

      return (T) newStrings;
    }

    return (T) truncateToSize((String) value, limit);
  }

  @Nullable
  private static String truncateToSize(@Nullable String s, int limit) {
    if (s == null || s.length() <= limit) {
      return s;
    }
    return s.substring(0, limit);
  }

  private StringUtils() {}
}
