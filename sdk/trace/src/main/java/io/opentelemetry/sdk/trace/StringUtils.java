/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.AttributeType;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

final class StringUtils {
  /**
   * If given attribute is of type STRING and has more characters than given {@code limit} then
   * return new value with string truncated to {@code limit} characters.
   *
   * <p>If given attribute is of type STRING_ARRAY and non-empty then return new value with every
   * element truncated to {@code limit} characters.
   *
   * <p>Otherwise return given {@code value}
   */
  @SuppressWarnings("unchecked")
  static <T> T truncateToSize(AttributeKey<T> key, T value, int limit) {
    if (value == null
        || ((key.getType() != AttributeType.STRING)
            && (key.getType() != AttributeType.STRING_ARRAY))) {
      return value;
    }

    if (key.getType() == AttributeType.STRING_ARRAY) {
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
