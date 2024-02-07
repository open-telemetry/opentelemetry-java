/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.incubator.trace;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;

/** Extended {@link Span} with experimental APIs. */
public interface ExtendedSpan extends Span {

  /**
   * Adds a link to this {@code Span}.
   *
   * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
   * operations, where a single batch handler processes multiple requests from different traces or
   * the same trace.
   *
   * <p>Implementations may ignore calls with an {@linkplain SpanContext#isValid() invalid span
   * context}.
   *
   * <p>Callers should prefer to add links before starting the span via {@link
   * SpanBuilder#addLink(SpanContext)} if possible.
   *
   * @param spanContext the context of the linked {@code Span}.
   * @return this.
   */
  default Span addLink(SpanContext spanContext) {
    return addLink(spanContext, Attributes.empty());
  }

  /**
   * Adds a link to this {@code Span}.
   *
   * <p>Links are used to link {@link Span}s in different traces. Used (for example) in batching
   * operations, where a single batch handler processes multiple requests from different traces or
   * the same trace.
   *
   * <p>Implementations may ignore calls with an {@linkplain SpanContext#isValid() invalid span
   * context}.
   *
   * <p>Callers should prefer to add links before starting the span via {@link
   * SpanBuilder#addLink(SpanContext, Attributes)} if possible.
   *
   * @param spanContext the context of the linked {@code Span}.
   * @param attributes the attributes of the {@code Link}.
   * @return this.
   */
  default Span addLink(SpanContext spanContext, Attributes attributes) {
    return this;
  }
}
