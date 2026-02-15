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

/**
 * A {@link TextMapGetter} that extracts context from a map carrier, intended for use with
 * environment variables.
 *
 * <p>Standard environment variable names are uppercase (e.g., {@code TRACEPARENT}, {@code
 * TRACESTATE}, {@code BAGGAGE}). This getter translates keys to uppercase before looking them up in
 * the carrier.
 */
public enum EnvironmentGetter implements TextMapGetter<Map<String, String>> {
  INSTANCE;

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
    // Spec recommends using uppercase for environment variable names.
    return carrier.get(key.toUpperCase(Locale.ROOT));
  }

  @Override
  public String toString() {
    return "EnvironmentGetter";
  }
}
