/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.propagation;

import io.opentelemetry.context.propagation.TextMapGetter;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A {@link TextMapGetter} that extracts context from a map carrier, intended for use with
 * environment variables.
 *
 * <p>Standard environment variable names are uppercase (e.g., {@code TRACEPARENT}, {@code
 * TRACESTATE}, {@code BAGGAGE}). This getter translates keys to uppercase and replaces characters
 * not allowed in environment variables (e.g., {@code .} and {@code -}) with underscores before
 * looking them up in the carrier.

 * @see <a href=
 *     "https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/context/env-carriers.md#format-restrictions">Environment
 *     Variable Format Restrictions</a>
 */
@Immutable
public final class EnvironmentGetter implements TextMapGetter<Map<String, String>> {

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
    return carrier.get(sanitizedKey);
  }

  @Override
  public String toString() {
    return "EnvironmentGetter";
  }
}
