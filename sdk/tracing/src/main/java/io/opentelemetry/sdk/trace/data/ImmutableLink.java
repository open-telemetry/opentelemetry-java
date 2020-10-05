/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.trace.SpanContext;
import javax.annotation.concurrent.Immutable;

/** An immutable implementation of {@link SpanData.Link}. */
@Immutable
@AutoValue
public abstract class ImmutableLink implements SpanData.Link {

  private static final Attributes DEFAULT_ATTRIBUTE_COLLECTION = Attributes.empty();
  private static final int DEFAULT_ATTRIBUTE_COUNT = 0;

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @return a new immutable {@code Event<T>}
   */
  public static ImmutableLink create(SpanContext spanContext) {
    return new AutoValue_ImmutableLink(
        spanContext, DEFAULT_ATTRIBUTE_COLLECTION, DEFAULT_ATTRIBUTE_COUNT);
  }

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @param attributes the attributes of this {@code Link}.
   * @return a new immutable {@code Event<T>}
   */
  public static ImmutableLink create(SpanContext spanContext, Attributes attributes) {
    return new AutoValue_ImmutableLink(spanContext, attributes, attributes.size());
  }

  /**
   * Returns a new immutable {@code Link}.
   *
   * @param spanContext the {@code SpanContext} of this {@code Link}.
   * @param attributes the attributes of this {@code Link}.
   * @param totalAttributeCount the total number of attributed for this {@code Link}.
   * @return a new immutable {@code Event<T>}
   */
  public static ImmutableLink create(
      SpanContext spanContext, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_ImmutableLink(spanContext, attributes, totalAttributeCount);
  }

  ImmutableLink() {}
}
