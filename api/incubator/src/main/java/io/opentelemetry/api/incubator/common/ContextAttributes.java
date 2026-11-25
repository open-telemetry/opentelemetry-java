/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.impl.ApiUsageLogger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;

// TODO (jack-berg): delete after 2027-07-01 if progress hasn't been made on
// https://github.com/open-telemetry/opentelemetry-specification/pull/4931
/**
 * Utility for associating {@link Attributes} with a {@link Context} for automatic propagation to
 * telemetry signals produced within that context.
 *
 * <p>SDKs can be configured to stamp context-scoped attributes across telemetry signals, with the
 * ability to filter based on signal, instrumentation scope, and attribute key.
 *
 * <h2>Cardinality Considerations</h2>
 *
 * <p>The cardinality of context-scoped attributes requires special consideration with metrics,
 * since metric cardinality limits can quickly be exceeded by high-cardinality attributes, putting
 * the metric into an error state.
 *
 * <h2>Bound Instruments</h2>
 *
 * <p>Bound instruments do not inherit dynamic context-scoped attributes.
 */
public final class ContextAttributes {

  private static final ContextKey<Attributes> CONTEXT_ATTRIBUTES_KEY =
      ContextKey.named("opentelemetry-context-attributes");

  private ContextAttributes() {}

  /**
   * Sets the {@link Attributes} on the given {@link Context}, completely replacing any previously
   * associated context-scoped attributes.
   *
   * @param context the context to attach attributes to.
   * @param attributes the authoritative attributes to associate. Pass {@link Attributes#empty()} to
   *     logically clear context-scoped attributes.
   * @return a new context containing the specified attributes.
   */
  public static Context set(Context context, Attributes attributes) {
    if (context == null) {
      ApiUsageLogger.logNullParam(ContextAttributes.class, "set", "context");
      return Context.root();
    }
    if (attributes == null) {
      ApiUsageLogger.logNullParam(ContextAttributes.class, "set", "attributes");
      return context;
    }
    if (attributes.isEmpty()) {
      Attributes existing = context.get(CONTEXT_ATTRIBUTES_KEY);
      if (existing == null || existing.isEmpty()) {
        return context;
      }
      return context.with(CONTEXT_ATTRIBUTES_KEY, Attributes.empty());
    }
    return context.with(CONTEXT_ATTRIBUTES_KEY, attributes);
  }

  /**
   * Returns the {@link Attributes} associated with the given {@code context}, or {@link
   * Attributes#empty()} if none are present.
   *
   * <p>This method is intended primarily for telemetry pipeline components such as SDKs, samplers,
   * processors, and exporters. Application code should treat context-scoped attributes as
   * write-only via {@link #set(Context, Attributes)}.
   */
  public static Attributes get(Context context) {
    if (context == null) {
      return Attributes.empty();
    }
    Attributes attributes = context.get(CONTEXT_ATTRIBUTES_KEY);
    return attributes != null ? attributes : Attributes.empty();
  }
}
