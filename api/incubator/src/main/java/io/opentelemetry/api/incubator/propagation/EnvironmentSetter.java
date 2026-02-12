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
 * TRACESTATE}, {@code BAGGAGE}). This setter translates keys to uppercase before inserting them
 * into the carrier.
 */
public enum EnvironmentSetter implements TextMapSetter<Map<String, String>> {
  INSTANCE;

  @Override
  public void set(@Nullable Map<String, String> carrier, String key, String value) {
    if (carrier == null || key == null || value == null) {
      return;
    }
    // Spec recommends using uppercase for environment variable names.
    carrier.put(key.toUpperCase(Locale.ROOT), value);
  }

  @Override
  public String toString() {
    return "EnvironmentSetter";
  }
}
