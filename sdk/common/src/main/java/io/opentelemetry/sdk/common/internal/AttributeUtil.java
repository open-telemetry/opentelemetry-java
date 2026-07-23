/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.common.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.AttributeValueLimits;
import java.util.Map;

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
      boolean allValidLength = true;
      for (Object value : attributes.asMap().values()) {
        if (!AttributeValueLimits.isValidLength(value, lengthLimit)) {
          allValidLength = false;
          break;
        }
      }
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
          (AttributeKey) entry.getKey(),
          AttributeValueLimits.applyLengthLimit(entry.getValue(), lengthLimit));
      i++;
    }
    return result.build();
  }
}
