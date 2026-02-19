/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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
 *   <li>Converts keys to uppercase (e.g., {@code traceparent} becomes {@code TRACEPARENT})
 *   <li>Replaces {@code .} and {@code -} with underscores
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

  private static final Logger logger = Logger.getLogger(EnvironmentSetter.class.getName());
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
    if (!isValidHttpHeaderValue(value)) {
      logger.log(
          Level.FINE,
          "Skipping environment variable injection for key ''{0}'': "
              + "value contains characters not valid in HTTP header fields per RFC 9110.",
          key);
      return;
    }
    // Spec recommends using uppercase and underscores for environment variable
    // names for maximum
    // cross-platform compatibility.
    String sanitizedKey = key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    carrier.put(sanitizedKey, value);
  }

  /**
   * Checks whether a string contains only characters valid in HTTP header field values per <a
   * href="https://datatracker.ietf.org/doc/html/rfc9110#section-5.5">RFC 9110 Section 5.5</a>.
   * Valid characters are: visible ASCII (0x21-0x7E), space (0x20), and horizontal tab (0x09).
   */
  static boolean isValidHttpHeaderValue(String value) {
    for (int i = 0; i < value.length(); i++) {
      char ch = value.charAt(i);
      // VCHAR (0x21-0x7E), SP (0x20), HTAB (0x09)
      if (ch != '\t' && (ch < ' ' || ch > '~')) {
        return false;
      }
    }
    return true;
  }

  @Override
  public String toString() {
    return "EnvironmentSetter";
  }
}
