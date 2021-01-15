/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.trace.samplers.Sampler;

/** Builder for {@link TraceConfig}. */
public final class TraceConfigBuilder {
  private static final Sampler DEFAULT_SAMPLER = Sampler.parentBased(Sampler.alwaysOn());
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 32;
  private static final int DEFAULT_MAX_ATTRIBUTE_LENGTH = TraceConfig.UNLIMITED_ATTRIBUTE_LENGTH;

  private Sampler sampler = DEFAULT_SAMPLER;
  private int maxNumAttributes = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
  private int maxNumEvents = DEFAULT_SPAN_MAX_NUM_EVENTS;
  private int maxNumLinks = DEFAULT_SPAN_MAX_NUM_LINKS;
  private int maxNumAttributesPerEvent = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
  private int maxNumAttributesPerLink = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;
  private int maxAttributeLength = DEFAULT_MAX_ATTRIBUTE_LENGTH;

  TraceConfigBuilder() {}

  /**
   * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link #build()}
   * will throw an exception.
   *
   * @param sampler the global default {@code Sampler}.
   * @return this.
   */
  public TraceConfigBuilder setSampler(Sampler sampler) {
    requireNonNull(sampler, "sampler");
    this.sampler = sampler;
    return this;
  }

  /**
   * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link #build()}
   * will throw an exception.
   *
   * @param samplerRatio the global default ratio used to make decisions on {@link Span} sampling.
   * @return this.
   */
  public TraceConfigBuilder setTraceIdRatioBased(double samplerRatio) {
    Utils.checkArgument(samplerRatio >= 0, "samplerRatio must be greater than or equal to 0.");
    Utils.checkArgument(samplerRatio <= 1, "samplerRatio must be lesser than or equal to 1.");
    if (samplerRatio == 1) {
      setSampler(Sampler.parentBased(Sampler.alwaysOn()));
    } else if (samplerRatio == 0) {
      setSampler(Sampler.alwaysOff());
    } else {
      setSampler(Sampler.parentBased(Sampler.traceIdRatioBased(samplerRatio)));
    }
    return this;
  }

  /**
   * Sets the global default max number of attributes per {@link Span}.
   *
   * @param maxNumberOfAttributes the global default max number of attributes per {@link Span}. It
   *     must be positive otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public TraceConfigBuilder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
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
  public TraceConfigBuilder setMaxNumberOfEvents(int maxNumberOfEvents) {
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
  public TraceConfigBuilder setMaxNumberOfLinks(int maxNumberOfLinks) {
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
  public TraceConfigBuilder setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent) {
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
  public TraceConfigBuilder setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink) {
    Utils.checkArgument(
        maxNumberOfAttributesPerLink > 0, "maxNumberOfAttributesPerLink must be greater than 0");
    this.maxNumAttributesPerLink = maxNumberOfAttributesPerLink;
    return this;
  }

  /**
   * Sets the global default max length of string attribute value in characters.
   *
   * @param maxLengthOfAttributeValues the global default max length of string attribute value in
   *     characters. It must be non-negative (or {@link TraceConfig#UNLIMITED_ATTRIBUTE_LENGTH})
   *     otherwise {@link #build()} will throw an exception.
   * @return this.
   */
  public TraceConfigBuilder setMaxLengthOfAttributeValues(int maxLengthOfAttributeValues) {
    Utils.checkArgument(
        maxLengthOfAttributeValues == -1 || maxLengthOfAttributeValues > 0,
        "maxLengthOfAttributeValues must be -1 to "
            + "disable length restriction, or positive to enable length restriction");
    this.maxAttributeLength = maxLengthOfAttributeValues;
    return this;
  }

  /**
   * Builds and returns a {@code TraceConfig} with the desired values.
   *
   * @return a {@code TraceConfig} with the desired values.
   * @throws IllegalArgumentException if any of the max numbers are not positive.
   */
  public TraceConfig build() {
    return TraceConfig.create(
        sampler,
        maxNumAttributes,
        maxNumEvents,
        maxNumLinks,
        maxNumAttributesPerEvent,
        maxNumAttributesPerLink,
        maxAttributeLength);
  }
}
