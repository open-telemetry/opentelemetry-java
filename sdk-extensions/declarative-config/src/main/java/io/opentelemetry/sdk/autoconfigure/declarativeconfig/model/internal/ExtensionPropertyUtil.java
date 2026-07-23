/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * Shared {@code @JsonAnySetter} logic for generated stable model classes.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class ExtensionPropertyUtil {

  private static final String DEV_SUFFIX = "/development";
  private static final Logger LOGGER = Logger.getLogger(ExtensionPropertyUtil.class.getName());

  private ExtensionPropertyUtil() {}

  /**
   * Routes a single {@code @JsonAnySetter} call into {@code extensionProperties}. Known
   * experimental keys are type-converted for parse-time checking. Keys of the form {@code
   * {stableName}/development} for a known stable property are graduated: a warning is logged and
   * the value is converted to the stable type but stored under the original {@code /development}
   * key so serialization round-trips. Unknown keys are stored raw on open types or
   * warned-and-dropped on closed types.
   */
  public static void handleAnySetter(
      String name,
      @Nullable Object value,
      Map<String, Object> extensionProperties,
      Map<String, Class<?>> experimentalPropertyTypes,
      Map<String, Class<?>> stablePropertyTypes,
      boolean allowsAdditionalProperties) {

    if (name.endsWith(DEV_SUFFIX)) {
      String candidate = name.substring(0, name.length() - DEV_SUFFIX.length());
      Class<?> stableType = stablePropertyTypes.get(candidate);
      if (stableType != null) {
        LOGGER.warning(
            "Property '" + name + "' has been stabilized. Use '" + candidate + "' instead.");
        extensionProperties.put(
            name, value == null ? null : ModelMapper.MAPPER.convertValue(value, stableType));
        return;
      }
    }

    Class<?> experimentalType = experimentalPropertyTypes.get(name);
    if (experimentalType != null) {
      extensionProperties.put(
          name, value == null ? null : ModelMapper.MAPPER.convertValue(value, experimentalType));
      return;
    }

    if (allowsAdditionalProperties) {
      extensionProperties.put(name, value);
      return;
    }

    LOGGER.warning("Unknown property '" + name + "' is not recognized and will be ignored.");
  }

  /**
   * Returns the extension property value for {@code key}, cast to {@code type}.
   *
   * <p>Values are guaranteed to be the correct type at store time: {@link #handleAnySetter}
   * converts known experimental properties via {@code ModelMapper} before storing, and the
   * generated accessor builders enforce the type at the call site. A {@link ClassCastException}
   * here indicates a contract violation (e.g. writing directly to the extension properties map with
   * the wrong type).
   */
  @Nullable
  public static <T> T get(String key, Map<String, Object> extensionProperties, Class<T> type) {
    Object raw = extensionProperties.get(key);
    return raw == null ? null : type.cast(raw);
  }

  /**
   * Looks up a graduated value stored by {@link #handleAnySetter} under {@code stableKey +
   * "/development"}.
   */
  @Nullable
  public static <T> T getGraduated(
      String stableKey, Map<String, Object> extensionProperties, Class<T> type) {
    return get(stableKey + DEV_SUFFIX, extensionProperties, type);
  }

  /**
   * Returns {@code extensionProperties} minus graduated entries (keys of the form {@code
   * X/development} where {@code X} is in {@code stableProperties}). Used by {@code @JsonAnyGetter}
   * to avoid emitting graduated values twice: once under the stable {@code @JsonProperty} key via
   * the getter's {@link #getGraduated} fallback, and once under {@code /development} via the raw
   * map.
   */
  public static Map<String, Object> filterSerializable(
      Map<String, Object> extensionProperties, Map<String, Class<?>> stableProperties) {
    if (stableProperties.isEmpty() || extensionProperties.isEmpty()) {
      return extensionProperties;
    }
    Map<String, Object> filtered = null;
    for (String key : extensionProperties.keySet()) {
      if (key.endsWith(DEV_SUFFIX)
          && stableProperties.containsKey(key.substring(0, key.length() - DEV_SUFFIX.length()))) {
        if (filtered == null) {
          filtered = new LinkedHashMap<>(extensionProperties);
        }
        filtered.remove(key);
      }
    }
    return filtered != null ? filtered : extensionProperties;
  }
}
