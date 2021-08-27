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
public abstract class SpanLimits {

  static final int DEFAULT_SPAN_MAX_ATTRIBUTE_LENGTH = Integer.MAX_VALUE;

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
      int maxNumAttributes,
      int maxNumEvents,
      int maxNumLinks,
      int maxNumAttributesPerEvent,
      int maxNumAttributesPerLink,
      int maxAttributeLength) {
    return new AutoValue_SpanLimits_SpanLimitsValue(
        maxNumAttributes,
        maxNumEvents,
        maxNumLinks,
        maxNumAttributesPerEvent,
        maxNumAttributesPerLink,
        maxAttributeLength);
  }

  /**
   * Create an instance.
   *
   * @deprecated Will be made package private in 2.0.0.
   */
  @Deprecated
  protected SpanLimits() {}

  /**
   * Returns the max number of attributes per {@link Span}.
   *
   * @return the max number of attributes per {@link Span}.
   */
  public abstract int getMaxNumberOfAttributes();

  /**
   * Returns the max number of events per {@link Span}.
   *
   * @return the max number of events per {@code Span}.
   */
  public abstract int getMaxNumberOfEvents();

  /**
   * Returns the max number of links per {@link Span}.
   *
   * @return the max number of links per {@code Span}.
   */
  public abstract int getMaxNumberOfLinks();

  /**
   * Returns the max number of attributes per event.
   *
   * @return the max number of attributes per event.
   */
  public abstract int getMaxNumberOfAttributesPerEvent();

  /**
   * Returns the max number of attributes per link.
   *
   * @return the max number of attributes per link.
   */
  public abstract int getMaxNumberOfAttributesPerLink();

  /**
   * Returns the max number of characters for string attribute values. For string array attributes
   * values, applies to each entry individually.
   *
   * @return the max number of characters for attribute strings.
   */
  public int getMaxAttributeValueLength() {
    return DEFAULT_SPAN_MAX_ATTRIBUTE_LENGTH;
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
        .setMaxNumberOfAttributes(getMaxNumberOfAttributes())
        .setMaxNumberOfEvents(getMaxNumberOfEvents())
        .setMaxNumberOfLinks(getMaxNumberOfLinks())
        .setMaxNumberOfAttributesPerEvent(getMaxNumberOfAttributesPerEvent())
        .setMaxNumberOfAttributesPerLink(getMaxNumberOfAttributesPerLink())
        .setMaxAttributeLength(getMaxAttributeValueLength());
  }

  @AutoValue
  @Immutable
  abstract static class SpanLimitsValue extends SpanLimits {

    /**
     * Override {@link SpanLimits#getMaxAttributeValueLength()} to be abstract so autovalue can
     * implement it.
     */
    @Override
    public abstract int getMaxAttributeValueLength();
  }
}
