/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class AttributeUtil {

  private AttributeUtil() {}

  /**
   * Apply the {@code countLimit} and {@code lengthLimit} to the attributes.
   *
   * <p>If all attributes fall within the limits, return as is. Else, return an attributes instance
   * with the limits applied. {@code countLimit} limits the number of unique attribute keys. {@code
   * lengthLimit} limits the length of attribute string and string list values.
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Attributes applyAttributesLimit(
      Attributes attributes, int countLimit, int lengthLimit) {
    if (attributes.isEmpty() || attributes.size() <= countLimit) {
      if (lengthLimit == Integer.MAX_VALUE) {
        return attributes;
      }
      boolean allValidLength =
          allMatch(attributes.asMap().values(), value -> isValidLength(value, lengthLimit));
      if (allValidLength) {
        return attributes;
      }
    }

    AttributesBuilder result = Attributes.builder();
    int i = 0;
    for (Map.Entry<AttributeKey<?>, Object> entry : attributes.asMap().entrySet()) {
      if (i >= countLimit) {
        break;
      }
      result.put(
          (AttributeKey) entry.getKey(), applyAttributeLengthLimit(entry.getValue(), lengthLimit));
      i++;
    }
    return result.build();
  }

  private static boolean isValidLength(Object value, int lengthLimit) {
    if (value instanceof List) {
      return allMatch((List<?>) value, entry -> isValidLength(entry, lengthLimit));
    } else if (value instanceof String) {
      return ((String) value).length() < lengthLimit;
    }
    return true;
  }

  private static <T> boolean allMatch(Iterable<T> iterable, Predicate<T> predicate) {
    for (T value : iterable) {
      if (!predicate.test(value)) {
        return false;
      }
    }
    return true;
  }

  /**
   * Apply the {@code lengthLimit} to the attribute {@code value}. Strings and strings in lists
   * which exceed the length limit are truncated.
   */
  public static Object applyAttributeLengthLimit(Object value, int lengthLimit) {
    if (lengthLimit == Integer.MAX_VALUE) {
      return value;
    }
    if (value instanceof List) {
      List<?> values = (List<?>) value;
      List<Object> response = new ArrayList<>(values.size());
      for (Object entry : values) {
        response.add(applyAttributeLengthLimit(entry, lengthLimit));
      }
      return response;
    }
    if (value instanceof String) {
      String str = (String) value;
      return str.length() < lengthLimit ? value : str.substring(0, lengthLimit);
    }
    return value;
  }
}
