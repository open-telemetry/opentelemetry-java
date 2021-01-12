/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.sdk.trace.config.TraceConfig;

/**
 * Data representation of a Link.
 *
 * <p>Used (for example) in batching operations, where a single batch handler processes multiple
 * requests from different traces. Link can be also used to reference spans from the same trace.
 */
public interface LinkData {

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @return a new immutable {@code Event<T>}
   */
  static LinkData create(SpanContext spanContext) {
    return ImmutableLinkData.create(spanContext);
  }

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @param attributes the attributes of this {@code Link}.
   * @return a new immutable {@code Event<T>}
   */
  static LinkData create(SpanContext spanContext, Attributes attributes) {
    return ImmutableLinkData.create(spanContext, attributes);
  }

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @param attributes the attributes of this {@code Link}.
   * @param totalAttributeCount the total number of attributed for this {@code Link}.
   * @return a new immutable {@code Event<T>}
   */
  static LinkData create(SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
    return ImmutableLinkData.create(spanContext, attributes, totalAttributeCount);
  }

  /** Returns the {@code SpanContext} of the span this {@link LinkData} refers to. */
  SpanContext getSpanContext();

  /**
   * Returns the set of attributes.
   *
   * @return the set of attributes.
   */
  Attributes getAttributes();

  /**
   * The total number of attributes that were recorded on this Link. This number may be larger than
   * the number of attributes that are attached to this span, if the total number recorded was
   * greater than the configured maximum value. See: {@link
   * TraceConfig#getMaxNumberOfAttributesPerLink()}
   *
   * @return The number of attributes on this link.
   */
  int getTotalAttributeCount();
}
