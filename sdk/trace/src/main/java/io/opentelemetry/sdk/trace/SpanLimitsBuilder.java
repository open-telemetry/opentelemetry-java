/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.trace.Span;

/** Builder for {@link SpanLimits}. */
public final class SpanLimitsBuilder {

  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 128;

  private int maxNumAttributes = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
  private int maxNumEvents = DEFAULT_SPAN_MAX_NUM_EVENTS;
  private int maxNumLinks = DEFAULT_SPAN_MAX_NUM_LINKS;
  private int maxNumAttributesPerEvent = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
  private int maxNumAttributesPerLink = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;
  private int maxAttributeLength = SpanLimits.DEFAULT_SPAN_MAX_ATTRIBUTE_LENGTH;

  SpanLimitsBuilder() {}

  /**
   * Sets the max number of attributes per {@link Span}.
   *
   * @param maxNumberOfAttributes the max number of attributes per {@link Span}. Must be positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfAttributes} is not positive.
   */
  public SpanLimitsBuilder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
    Utils.checkArgument(maxNumberOfAttributes > 0, "maxNumberOfAttributes must be greater than 0");
    this.maxNumAttributes = maxNumberOfAttributes;
    return this;
  }

  /**
   * Sets the max number of events per {@link Span}.
   *
   * @param maxNumberOfEvents the max number of events per {@link Span}. Must be positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfEvents} is not positive.
   */
  public SpanLimitsBuilder setMaxNumberOfEvents(int maxNumberOfEvents) {
    Utils.checkArgument(maxNumberOfEvents > 0, "maxNumberOfEvents must be greater than 0");
    this.maxNumEvents = maxNumberOfEvents;
    return this;
  }

  /**
   * Sets the max number of links per {@link Span}.
   *
   * @param maxNumberOfLinks the max number of links per {@link Span}. Must be positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfLinks} is not positive.
   */
  public SpanLimitsBuilder setMaxNumberOfLinks(int maxNumberOfLinks) {
    Utils.checkArgument(maxNumberOfLinks > 0, "maxNumberOfLinks must be greater than 0");
    this.maxNumLinks = maxNumberOfLinks;
    return this;
  }

  /**
   * Sets the max number of attributes per event.
   *
   * @param maxNumberOfAttributesPerEvent the max number of attributes per event. Must be positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfAttributesPerEvent} is not positive.
   */
  public SpanLimitsBuilder setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent) {
    Utils.checkArgument(
        maxNumberOfAttributesPerEvent > 0, "maxNumberOfAttributesPerEvent must be greater than 0");
    this.maxNumAttributesPerEvent = maxNumberOfAttributesPerEvent;
    return this;
  }

  /**
   * Sets the max number of attributes per link.
   *
   * @param maxNumberOfAttributesPerLink the max number of attributes per link. Must be positive.
   * @return this.
   * @throws IllegalArgumentException if {@code maxNumberOfAttributesPerLink} is not positive.
   */
  public SpanLimitsBuilder setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink) {
    Utils.checkArgument(
        maxNumberOfAttributesPerLink > 0, "maxNumberOfAttributesPerLink must be greater than 0");
    this.maxNumAttributesPerLink = maxNumberOfAttributesPerLink;
    return this;
  }

  /**
   * Sets the max number of characters for attribute strings. For string array attributes, applies
   * to each entry individually.
   *
   * @param maxAttributeLength the max characters for attribute strings. Must not be negative.
   * @return this.
   * @throws IllegalArgumentException if {@code maxAttributeLength} is negative.
   */
  public SpanLimitsBuilder setMaxAttributeLength(int maxAttributeLength) {
    Utils.checkArgument(maxAttributeLength > -1, "maxAttributeLength must be non-negative");
    this.maxAttributeLength = maxAttributeLength;
    return this;
  }

  /** Builds and returns a {@link SpanLimits} with the values of this builder. */
  public SpanLimits build() {
    return SpanLimits.create(
        maxNumAttributes,
        maxNumEvents,
        maxNumLinks,
        maxNumAttributesPerEvent,
        maxNumAttributesPerLink,
        maxAttributeLength);
  }
}
