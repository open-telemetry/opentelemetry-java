/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A {@link TextMapSetter} that injects context into a map carrier, intended for use with
 * environment variables when spawning child processes.
 *
 * <p>This is useful when an application needs to propagate context to sub-processes via their
 * environment. For example, when using {@link ProcessBuilder}:
 *
 * <pre>{@code
 * Map<String, String> env = new HashMap<>();
 * contextPropagators.getTextMapPropagator().inject(context, env, EnvironmentSetter.getInstance());
 * ProcessBuilder processBuilder = new ProcessBuilder();
 * processBuilder.environment().putAll(env);
 * }</pre>
 *
 * <p>This setter automatically sanitizes keys to be compatible with environment variable naming
 * conventions:
 *
 * <ul>
 *   <li>ASCII letters are converted to uppercase
 *   <li>Any character that is not an ASCII letter, digit, or underscore is replaced with an
 *       underscore
 *   <li>If the result would start with a digit, an underscore is prepended
 * </ul>
 *
 * <p>Values are validated to contain only characters valid in HTTP header fields per <a
 * href="https://datatracker.ietf.org/doc/html/rfc9110#section-5.5">RFC 9110</a> (visible ASCII
 * characters, space, and horizontal tab). Values containing invalid characters are silently
 * skipped.
 *
 * <p><strong>Size limitations:</strong> Environment variable sizes are platform-dependent (e.g.,
 * Windows limits name=value pairs to 32,767 characters). Callers are responsible for being aware of
 * platform-specific limits when injecting context.
 *
 * @see <a href=
 *     "https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/context/env-carriers.md#format-restrictions">Environment
 *     Variable Format Restrictions</a>
 */
public final class EnvironmentSetter implements TextMapSetter<Map<String, String>> {

  private static final EnvironmentSetter INSTANCE = new EnvironmentSetter();

  private EnvironmentSetter() {}

  /** Returns the singleton instance of {@link EnvironmentSetter}. */
  public static EnvironmentSetter getInstance() {
    return INSTANCE;
  }

  @Override
  public void set(@Nullable Map<String, String> carrier, String key, String value) {
    if (carrier == null || key == null || value == null) {
      return;
    }
    String normalizedKey = normalizeKey(key);
    carrier.put(normalizedKey, value);
  }

  /**
   * Normalizes a key to be a valid environment variable name.
   *
   * <ul>
   *   <li>ASCII letters are converted to uppercase
   *   <li>Any character that is not an ASCII letter, digit, or underscore is replaced with an
   *       underscore (including {@code .}, {@code -}, whitespace, and control characters)
   *   <li>If the result would start with a digit, an underscore is prepended
   * </ul>
   */
  static String normalizeKey(String key) {
    StringBuilder sb = new StringBuilder(key.length() + 1);
    for (int i = 0; i < key.length(); i++) {
      char ch = key.charAt(i);
      if (ch >= 'a' && ch <= 'z') {
        sb.append((char) (ch - 32));
      } else if ((ch >= 'A' && ch <= 'Z') || (ch >= '0' && ch <= '9') || ch == '_') {
        sb.append(ch);
      } else {
        sb.append('_');
      }
    }
    if (sb.length() > 0 && sb.charAt(0) >= '0' && sb.charAt(0) <= '9') {
      sb.insert(0, '_');
    }
    return sb.toString();
  }

  @Override
  public String toString() {
    return "EnvironmentSetter";
  }
}
