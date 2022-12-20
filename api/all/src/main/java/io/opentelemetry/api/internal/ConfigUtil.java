/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.internal;

import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Configuration utilities.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
public final class ConfigUtil {

  private ConfigUtil() {}

  /**
   * Return the system property or environment variable for the {@code key}.
   *
   * <p>Normalize the {@code key} using {@link #normalizePropertyKey(String)}. Match to system
   * property keys also normalized with {@link #normalizePropertyKey(String)}. Match to environment
   * variable keys normalized with {@link #normalizeEnvironmentVariableKey(String)}. System
   * properties take priority over environment variables.
   *
   * @param key the property key
   * @return the system property if not null, or the environment variable if not null, or {@code
   *     defaultValue}
   */
  public static String getString(String key, String defaultValue) {
    String normalizedKey = normalizePropertyKey(key);
    String systemProperty =
        System.getProperties().entrySet().stream()
            .filter(entry -> normalizedKey.equals(normalizePropertyKey(entry.getKey().toString())))
            .map(entry -> entry.getValue().toString())
            .findFirst()
            .orElse(null);
    if (systemProperty != null) {
      return systemProperty;
    }
    return System.getenv().entrySet().stream()
        .filter(entry -> normalizedKey.equals(normalizeEnvironmentVariableKey(entry.getKey())))
        .map(Map.Entry::getValue)
        .findFirst()
        .orElse(defaultValue);
  }

  /**
   * Normalize an environment variable key by converting to lower case and replacing "_" with ".".
   */
  public static String normalizeEnvironmentVariableKey(String key) {
    return key.toLowerCase(Locale.ROOT).replace("_", ".");
  }

  /** Normalize a property key by converting to lower case and replacing "-" with ".". */
  public static String normalizePropertyKey(String key) {
    return key.toLowerCase(Locale.ROOT).replace("-", ".");
  }

  /** Returns defaultValue if value is null, otherwise value. This is an internal method. */
  public static <T> T defaultIfNull(@Nullable T value, T defaultValue) {
    return value == null ? defaultValue : value;
  }
}
