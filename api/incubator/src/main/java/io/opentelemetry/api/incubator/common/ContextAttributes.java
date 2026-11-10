/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.impl.ApiUsageLogger;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import javax.annotation.Nullable;

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
 * <p>Attaching high-cardinality attributes to {@link Context} poses a critical risk to metrics.
 * Synchronous metric recordings inherit these attributes into their metric point dimension sets,
 * which can cause metric stream explosion and high memory consumption. Therefore, attributes
 * associated with {@link Context} <b>must</b> be low-cardinality unless a filter is explicitly
 * configured on the metric provider to exclude them.
 *
 * <h2>Bound Instruments</h2>
 *
 * <p>Bound instruments do not inherit dynamic context-scoped attributes. Their attribute sets are
 * resolved once at bind time, and subsequent bound recording operations do not access or evaluate
 * the active context.
 */
public final class ContextAttributes {

  private static final ContextKey<Attributes> CONTEXT_ATTRIBUTES_KEY =
      ContextKey.named("opentelemetry-context-attributes");

  private ContextAttributes() {}

  /**
   * Sets the {@link Attributes} on the given {@link Context}, completely replacing any previously
   * associated context-scoped attributes.
   *
   * @param context the context to attach attributes to. If null, {@link Context#root()} is
   *     returned.
   * @param attributes the authoritative attributes to associate. If null, {@code context} is
   *     returned unchanged. Pass {@link Attributes#empty()} to logically clear context-scoped
   *     attributes.
   * @return a new context containing the specified attributes.
   */
  public static Context set(@Nullable Context context, @Nullable Attributes attributes) {
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
   * Adds the given {@link Attributes} to the {@link Context}, merging them with any existing
   * context-scoped attributes. Conflicting keys take the value from {@code attributes}.
   *
   * @param context the context to attach attributes to. If null, {@link Context#root()} is
   *     returned.
   * @param attributes the attributes to add. If null or empty, {@code context} is returned
   *     unchanged.
   * @return a new context containing the merged attributes.
   */
  public static Context add(@Nullable Context context, @Nullable Attributes attributes) {
    if (context == null) {
      ApiUsageLogger.logNullParam(ContextAttributes.class, "add", "context");
      return Context.root();
    }
    if (attributes == null) {
      ApiUsageLogger.logNullParam(ContextAttributes.class, "add", "attributes");
      return context;
    }
    if (attributes.isEmpty()) {
      return context;
    }
    Attributes existing = get(context);
    if (existing.isEmpty()) {
      return set(context, attributes);
    }
    return set(context, existing.toBuilder().putAll(attributes).build());
  }

  /**
   * Returns the {@link Attributes} associated with the given {@code context}, or {@link
   * Attributes#empty()} if none are present.
   *
   * <p>This method is intended primarily for telemetry pipeline components such as SDKs, samplers,
   * processors, and exporters. Application code should treat context-scoped attributes as
   * write-only via {@link #set(Context, Attributes)} or {@link #add(Context, Attributes)}.
   */
  public static Attributes get(@Nullable Context context) {
    if (context == null) {
      return Attributes.empty();
    }
    Attributes attributes = context.get(CONTEXT_ATTRIBUTES_KEY);
    return attributes != null ? attributes : Attributes.empty();
  }
}
