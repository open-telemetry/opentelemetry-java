/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.common;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ContextKey;
import javax.annotation.Nullable;

// TODO (jack-berg): delete after 2027-07-01 if progress hasn't been made on
// https://github.com/open-telemetry/opentelemetry-java/issues/8532
/**
 * Utility for associating {@link Attributes} with a {@link Context} for automatic propagation to
 * telemetry signals produced within that context.
 *
 * <h2>Usage and Authoring Guidance</h2>
 *
 * <p>Context-scoped attributes are designed for cross-cutting contextual identity attributes (such
 * as {@code tenant.id}, {@code account.id}, or deployment environment metadata) established at
 * request or execution boundaries (for example, in HTTP/RPC server interceptors or messaging
 * consumers). General library instrumentations should avoid populating context-scoped attributes to
 * prevent injecting library-internal concerns into ambient application telemetry.
 *
 * <h2>Cardinality Considerations</h2>
 *
 * <p>Context-scoped attributes are stamped across all active telemetry signals produced within the
 * context. While high or unbounded cardinality attributes (such as user identifiers or transaction
 * IDs) are acceptable on event-based signals like spans and log records, attaching high-cardinality
 * attributes to {@link Context} poses a critical risk to metrics. Synchronous metric recordings
 * inherit these attributes into their metric point dimension sets, which can cause metric stream
 * explosion and high memory consumption. Therefore, attributes associated with {@link Context}
 * <b>must</b> be low-cardinality unless a filter is explicitly configured on the metric provider
 * (via {@code SdkMeterProviderBuilder.setContextScopedAttributesFilter}) to exclude them.
 *
 * <h2>Bound Instruments</h2>
 *
 * <p>Bound instruments (such as {@code BoundLongCounter} or {@code BoundDoubleCounter}) do
 * <b>not</b> inherit context-scoped attributes. Their attribute sets are resolved once at {@code
 * bind()} time, and subsequent bound recording operations do not access or evaluate the active
 * context.
 */
public final class ContextAttributes {

  private static final ContextKey<Attributes> CONTEXT_ATTRIBUTES_KEY =
      ContextKey.named("opentelemetry-context-attributes");

  private ContextAttributes() {}

  /**
   * Sets the {@link Attributes} on the given {@link Context}, completely replacing any previously
   * associated context-scoped attributes.
   *
   * @param context the context to attach attributes to. Must not be null.
   * @param attributes the authoritative attributes to associate. Must not be null. Pass {@link
   *     Attributes#empty()} to logically clear context-scoped attributes.
   * @return a new context containing the specified attributes.
   */
  public static Context set(Context context, Attributes attributes) {
    requireNonNull(context, "context");
    requireNonNull(attributes, "attributes");
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
   * @param context the context to attach attributes to. Must not be null.
   * @param attributes the attributes to add. Must not be null. If empty, {@code context} is
   *     returned unchanged.
   * @return a new context containing the merged attributes.
   */
  public static Context add(Context context, Attributes attributes) {
    requireNonNull(context, "context");
    requireNonNull(attributes, "attributes");
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
