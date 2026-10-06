/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.resources.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.api.internal.StringUtils;
import io.opentelemetry.api.internal.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;

/**
 * Helpers to check resource entity attributes.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 *
 * @see SdkEntityBuilder
 */
public final class AttributeCheckUtil {
  private AttributeCheckUtil() {}

  // Note: Max length is actually configurable by specification.
  private static final int MAX_LENGTH = 255;
  private static final String ERROR_MESSAGE_INVALID_CHARS =
      " should be a ASCII string with a length greater than 0 and not exceed "
          + MAX_LENGTH
          + " characters.";
  private static final String ERROR_MESSAGE_INVALID_VALUE =
      " should be a ASCII string with a length not exceed " + MAX_LENGTH + " characters.";

  /** Determine if the set of attributes if valid for Resource / Entity. */
  public static void checkAttributes(Attributes attributes) {
    attributes.forEach(
        (key, value) -> {
          Utils.checkArgument(
              isValidAndNotEmpty(key), "Attribute key" + ERROR_MESSAGE_INVALID_CHARS);
          Objects.requireNonNull(value, "Attribute value" + ERROR_MESSAGE_INVALID_VALUE);
        });
  }

  /** Copies and freezes array values without changing attribute key types. */
  @SuppressWarnings({"unchecked", "rawtypes"})
  public static Attributes immutableCopy(Attributes attributes) {
    @Nullable AttributesBuilder copy = null;
    for (Map.Entry<AttributeKey<?>, Object> entry : attributes.asMap().entrySet()) {
      switch (entry.getKey().getType()) {
        case STRING_ARRAY:
        case LONG_ARRAY:
        case DOUBLE_ARRAY:
        case BOOLEAN_ARRAY:
          if (copy == null) {
            copy = attributes.toBuilder();
          }
          copy.put(
              (AttributeKey) entry.getKey(),
              Collections.unmodifiableList(new ArrayList<>((List<?>) entry.getValue())));
          break;
        default:
          break;
      }
    }
    return copy == null ? attributes : copy.build();
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length
   * greater than 0 and not exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  public static boolean isValidAndNotEmpty(AttributeKey<?> name) {
    return !name.getKey().isEmpty() && isValid(name.getKey());
  }

  /**
   * Determines whether the given {@code String} is a valid printable ASCII string with a length not
   * exceed {@link #MAX_LENGTH} characters.
   *
   * @param name the name to be validated.
   * @return whether the name is valid.
   */
  public static boolean isValid(String name) {
    return name.length() <= MAX_LENGTH && StringUtils.isPrintableString(name);
  }
}
