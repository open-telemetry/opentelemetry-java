/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.Span;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds limits enforced during span recording.
 *
 * <p>Note: To allow dynamic updates of {@link SpanLimits} you should register a {@link
 * java.util.function.Supplier} with {@link
 * io.opentelemetry.sdk.trace.SdkTracerProviderBuilder#setSpanLimits(java.util.function.Supplier)}
 * which supplies dynamic configs when queried.
 */
@AutoValue
@Immutable
public abstract class SpanLimits {

  /**
   * Value for attribute length which indicates attributes should not be truncated.
   *
   * @see SpanLimitsBuilder#setMaxLengthOfAttributeValues(int)
   */
  public static final int UNLIMITED_ATTRIBUTE_LENGTH = -1;

  // These values are the default values for all the global parameters.
  // TODO: decide which default sampler to use

  private static final SpanLimits DEFAULT = new SpanLimitsBuilder().build();

  /** Returns the default {@link SpanLimits}. */
  public static SpanLimits getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link SpanLimitsBuilder} to construct a {@link SpanLimits}. */
  public static SpanLimitsBuilder builder() {
    return new SpanLimitsBuilder();
  }

  static SpanLimits create(
      int spanAttributeLimit,
      int spanEventLimit,
      int spanLinkLimit,
      int eventAttributeLimit,
      int eventLinkLimit,
      int attributeValueLengthLimit) {
    return new AutoValue_SpanLimits(
        spanAttributeLimit,
        spanEventLimit,
        spanLinkLimit,
        eventAttributeLimit,
        eventLinkLimit,
        attributeValueLengthLimit);
  }

  /**
   * Returns the global default max number of attributes per {@link Span}.
   *
   * @return the global default max number of attributes per {@link Span}.
   */
  public abstract int getSpanAttributeLimit();

  /**
   * Returns the global default max number of events per {@link Span}.
   *
   * @return the global default max number of events per {@code Span}.
   */
  public abstract int getSpanEventLimit();

  /**
   * Returns the global default max number of links per {@link Span}.
   *
   * @return the global default max number of links per {@code Span}.
   */
  public abstract int getSpanLinkLimit();

  /**
   * Returns the global default max number of attributes per event.
   *
   * @return the global default max number of attributes per event.
   */
  public abstract int getEventAttributeLimit();

  /**
   * Returns the global default max number of attributes per link.
   *
   * @return the global default max number of attributes per link.
   */
  public abstract int getLinkAttributeLimit();

  /**
   * Returns the global default max length of string attribute value in characters.
   *
   * @return the global default max length of string attribute value in characters.
   * @see #shouldTruncateStringAttributeValues()
   */
  public abstract int getAttributeValueLengthLimit();

  public boolean shouldTruncateStringAttributeValues() {
    return getAttributeValueLengthLimit() != UNLIMITED_ATTRIBUTE_LENGTH;
  }

  /**
   * Returns a {@link SpanLimitsBuilder} initialized to the same property values as the current
   * instance.
   *
   * @return a {@link SpanLimitsBuilder} initialized to the same property values as the current
   *     instance.
   */
  public SpanLimitsBuilder toBuilder() {
    return new SpanLimitsBuilder()
        .setSpanAttributeLimit(getSpanAttributeLimit())
        .setSpanEventLimit(getSpanEventLimit())
        .setSpanLinkLimit(getSpanLinkLimit())
        .setMaxNumberOfAttributesPerEvent(getEventAttributeLimit())
        .setMaxNumberOfAttributesPerLink(getLinkAttributeLimit())
        .setMaxLengthOfAttributeValues(getAttributeValueLengthLimit());
  }
}
