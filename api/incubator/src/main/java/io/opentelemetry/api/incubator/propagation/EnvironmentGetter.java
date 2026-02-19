/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

/**
 * A {@link TextMapGetter} that extracts context from a map carrier, intended for use with
 * environment variables in child processes.
 *
 * <p>This is useful when a child process needs to extract propagated context from its environment.
 * For example:
 *
 * <pre>{@code
 * Map<String, String> env = System.getenv();
 * Context context = contextPropagators.getTextMapPropagator()
 *     .extract(Context.current(), env, EnvironmentGetter.getInstance());
 * }</pre>
 *
 * <p>This getter automatically sanitizes keys to match environment variable naming conventions:
 *
 * <ul>
 *   <li>Converts keys to uppercase (e.g., {@code traceparent} becomes {@code TRACEPARENT})
 *   <li>Replaces {@code .} and {@code -} with underscores
 * </ul>
 *
 * <p>Values are validated to contain only characters valid in HTTP header fields per <a
 * href="https://datatracker.ietf.org/doc/html/rfc9110#section-5.5">RFC 9110</a> (visible ASCII
 * characters, space, and horizontal tab). Values containing invalid characters are treated as
 * absent and {@code null} is returned.
 *
 * @see <a href=
 *     "https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/context/env-carriers.md#format-restrictions">Environment
 *     Variable Format Restrictions</a>
 */
public final class EnvironmentGetter implements TextMapGetter<Map<String, String>> {

  private static final Logger logger = Logger.getLogger(EnvironmentGetter.class.getName());
  private static final EnvironmentGetter INSTANCE = new EnvironmentGetter();

  private EnvironmentGetter() {}

  /** Returns the singleton instance of {@link EnvironmentGetter}. */
  public static EnvironmentGetter getInstance() {
    return INSTANCE;
  }

  @Override
  public Iterable<String> keys(Map<String, String> carrier) {
    if (carrier == null) {
      return Collections.emptyList();
    }
    return carrier.keySet();
  }

  @Nullable
  @Override
  public String get(@Nullable Map<String, String> carrier, String key) {
    if (carrier == null || key == null) {
      return null;
    }
    // Spec recommends using uppercase and underscores for environment variable
    // names for maximum
    // cross-platform compatibility.
    String sanitizedKey = key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    String value = carrier.get(sanitizedKey);
    if (value != null && !EnvironmentSetter.isValidHttpHeaderValue(value)) {
      logger.log(
          Level.FINE,
          "Ignoring environment variable '{0}': "
              + "value contains characters not valid in HTTP header fields per RFC 9110.",
          sanitizedKey);
      return null;
    }
    return value;
  }

  @Override
  public String toString() {
    return "EnvironmentGetter";
  }
}
