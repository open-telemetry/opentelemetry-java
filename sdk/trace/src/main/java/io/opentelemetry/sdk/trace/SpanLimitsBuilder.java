/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.trace.Span;

/** Builder for {@link SpanLimits}. */
public final class SpanLimitsBuilder {
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 32;
  private static final int DEFAULT_MAX_ATTRIBUTE_LENGTH = SpanLimits.UNLIMITED_ATTRIBUTE_LENGTH;

  private int maxNumAttributes = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
  private int maxNumEvents = DEFAULT_SPAN_MAX_NUM_EVENTS;
  private int maxNumLinks = DEFAULT_SPAN_MAX_NUM_LINKS;
  private int maxNumAttributesPerEvent = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
  private int maxNumAttributesPerLink = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;
  private int maxAttributeLength = DEFAULT_MAX_ATTRIBUTE_LENGTH;

  SpanLimitsBuilder() {}

  /**
   * Sets the global default max number of attributes per {@link Span}.
   *
   * @param maxNumberOfAttributes the global default max number of attributes per {@link Span}. It
   *     must be positive otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public SpanLimitsBuilder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
    Utils.checkArgument(maxNumberOfAttributes > 0, "maxNumberOfAttributes must be greater than 0");
    this.maxNumAttributes = maxNumberOfAttributes;
    return this;
  }

  /**
   * Sets the global default max number of events per {@link Span}.
   *
   * @param maxNumberOfEvents the global default max number of events per {@link Span}. It must be
   *     positive otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public SpanLimitsBuilder setMaxNumberOfEvents(int maxNumberOfEvents) {
    Utils.checkArgument(maxNumberOfEvents > 0, "maxNumberOfEvents must be greater than 0");
    this.maxNumEvents = maxNumberOfEvents;
    return this;
  }

  /**
   * Sets the global default max number of links per {@link Span}.
   *
   * @param maxNumberOfLinks the global default max number of links per {@link Span}. It must be
   *     positive otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public SpanLimitsBuilder setMaxNumberOfLinks(int maxNumberOfLinks) {
    Utils.checkArgument(maxNumberOfLinks > 0, "maxNumberOfLinks must be greater than 0");
    this.maxNumLinks = maxNumberOfLinks;
    return this;
  }

  /**
   * Sets the global default max number of attributes per event.
   *
   * @param maxNumberOfAttributesPerEvent the global default max number of attributes per event. It
   *     must be positive otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public SpanLimitsBuilder setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent) {
    Utils.checkArgument(
        maxNumberOfAttributesPerEvent > 0, "maxNumberOfAttributesPerEvent must be greater than 0");
    this.maxNumAttributesPerEvent = maxNumberOfAttributesPerEvent;
    return this;
  }

  /**
   * Sets the global default max number of attributes per link.
   *
   * @param maxNumberOfAttributesPerLink the global default max number of attributes per link. It
   *     must be positive otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public SpanLimitsBuilder setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink) {
    Utils.checkArgument(
        maxNumberOfAttributesPerLink > 0, "maxNumberOfAttributesPerLink must be greater than 0");
    this.maxNumAttributesPerLink = maxNumberOfAttributesPerLink;
    return this;
  }

  /**
   * Sets the global default max length of string attribute value in characters.
   *
   * @param maxLengthOfAttributeValues the global default max length of string attribute value in
   *     characters. It must be non-negative (or {@link SpanLimits#UNLIMITED_ATTRIBUTE_LENGTH})
   *     otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public SpanLimitsBuilder setMaxLengthOfAttributeValues(int maxLengthOfAttributeValues) {
    Utils.checkArgument(
        maxLengthOfAttributeValues == -1 || maxLengthOfAttributeValues > 0,
        "maxLengthOfAttributeValues must be -1 to "
            + "disable length restriction, or positive to enable length restriction");
    this.maxAttributeLength = maxLengthOfAttributeValues;
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
