/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;

/**
 * Utility for associating {@link Attributes} with a {@link Context} for automatic propagation to
 * telemetry signals produced within that context.
 */
public final class ContextAttributes {

  private static final ContextKey<Attributes> CONTEXT_ATTRIBUTES_KEY =
      ContextKey.named("opentelemetry-context-attributes");

  private ContextAttributes() {}

  /**
   * Returns a new {@link Context} containing the merged attributes from the given {@code context}
   * and {@code attributes}. If the same attribute key is present in both, the value from {@code
   * attributes} takes precedence.
   */
  public static Context add(Context context, Attributes attributes) {
    requireNonNull(context, "context");
    if (attributes == null || attributes.isEmpty()) {
      return context;
    }
    Attributes existing = context.get(CONTEXT_ATTRIBUTES_KEY);
    if (existing == null) {
      return context.with(CONTEXT_ATTRIBUTES_KEY, attributes);
    }
    Attributes merged = existing.toBuilder().putAll(attributes).build();
    return context.with(CONTEXT_ATTRIBUTES_KEY, merged);
  }

  /**
   * Returns the {@link Attributes} associated with the given {@code context}, or {@link
   * Attributes#empty()} if none are present.
   *
   * <p>This method is intended primarily for telemetry pipeline components such as SDKs, samplers,
   * processors, and exporters. Application code should treat context-scoped attributes as
   * write-only via {@link #add(Context, Attributes)}.
   */
  public static Attributes get(Context context) {
    if (context == null) {
      return Attributes.empty();
    }
    Attributes attributes = context.get(CONTEXT_ATTRIBUTES_KEY);
    return attributes != null ? attributes : Attributes.empty();
  }
}
