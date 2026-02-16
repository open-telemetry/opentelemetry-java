/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import io.opentelemetry.context.propagation.TextMapSetter;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * A {@link TextMapSetter} that injects context into a map carrier, intended for use with
 * environment variables.
 *
 * <p>Standard environment variable names are uppercase (e.g., {@code TRACEPARENT}, {@code
 * TRACESTATE}, {@code BAGGAGE}). This setter translates keys to uppercase and replaces characters
 * not allowed in environment variables (e.g., {@code .} and {@code -}) with underscores before
 * inserting them into the carrier.
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
    // Spec recommends using uppercase and underscores for environment variable
    // names for maximum
    // cross-platform compatibility.
    String sanitizedKey = key.replace('.', '_').replace('-', '_').toUpperCase(Locale.ROOT);
    carrier.put(sanitizedKey, value);
  }

  @Override
  public String toString() {
    return "EnvironmentSetter";
  }
}
