/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.trace.Span;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds global trace parameters.
 *
 * <p>Note: To allow dynamic updates of {@link TraceConfig} you should register a {@link
 * java.util.function.Supplier} with {@link
 * io.opentelemetry.sdk.trace.SdkTracerProviderBuilder#setTraceConfig(java.util.function.Supplier)}
 * which supplies dynamic configs when queried.
 *
 * <p>Configuration options for {@link TraceConfig} can be read from system properties, environment
 * variables, or {@link java.util.Properties} objects.
 *
 * <p>For system properties and {@link java.util.Properties} objects, {@link TraceConfig} will look
 * for the following names:
 *
 * <ul>
 *   <li>{@code otel.config.sampler.probability}: to set the global default sampler which is used
 *       when constructing a new {@code Span}.
 *   <li>{@code otel.span.attribute.count.limit}: to set the global default max number of attributes
 *       per {@link Span}.
 *   <li>{@code otel.span.event.count.limit}: to set the global default max number of events per
 *       {@link Span}.
 *   <li>{@code otel.span.link.count.limit}: to set the global default max number of links per
 *       {@link Span}.
 *   <li>{@code otel.config.max.event.attrs}: to set the global default max number of attributes per
 *       event.
 *   <li>{@code otel.config.max.link.attrs}: to set the global default max number of attributes per
 *       link.
 *   <li>{@code otel.config.max.attr.length}: to set the global default max length of string
 *       attribute value in characters.
 * </ul>
 *
 * <p>For environment variables, {@link TraceConfig} will look for the following names:
 *
 * <ul>
 *   <li>{@code OTEL_CONFIG_SAMPLER_PROBABILITY}: to set the global default sampler which is used
 *       when constructing a new {@code Span}.
 *   <li>{@code OTEL_SPAN_ATTRIBUTE_COUNT_LIMIT}: to set the global default max number of attributes
 *       per {@link Span}.
 *   <li>{@code OTEL_SPAN_EVENT_COUNT_LIMIT}: to set the global default max number of events per
 *       {@link Span}.
 *   <li>{@code OTEL_SPAN_LINK_COUNT_LIMIT}: to set the global default max number of links per
 *       {@link Span}.
 *   <li>{@code OTEL_CONFIG_MAX_EVENT_ATTRS}: to set the global default max number of attributes per
 *       event.
 *   <li>{@code OTEL_CONFIG_MAX_LINK_ATTRS}: to set the global default max number of attributes per
 *       link.
 *   <li>{@code OTEL_CONFIG_MAX_ATTR_LENGTH}: to set the global default max length of string
 *       attribute value in characters.
 * </ul>
 */
@AutoValue
@Immutable
public abstract class TraceConfig {

  /**
   * Value for attribute length which indicates attributes should not be truncated.
   *
   * @see TraceConfigBuilder#setMaxLengthOfAttributeValues(int)
   */
  public static final int UNLIMITED_ATTRIBUTE_LENGTH = -1;

  // These values are the default values for all the global parameters.
  // TODO: decide which default sampler to use

  private static final TraceConfig DEFAULT = new TraceConfigBuilder().build();

  /**
   * Returns the default {@code TraceConfig}.
   *
   * @return the default {@code TraceConfig}.
   */
  public static TraceConfig getDefault() {
    return DEFAULT;
  }

  /** Returns a new {@link TraceConfigBuilder} to construct a {@link TraceConfig}. */
  public static TraceConfigBuilder builder() {
    return new TraceConfigBuilder();
  }

  static TraceConfig create(
      int maxNumAttributes,
      int maxNumEvents,
      int maxNumLinks,
      int maxNumAttributesPerEvent,
      int maxNumAttributesPerLink,
      int maxAttributeLength) {
    return new AutoValue_TraceConfig(
        maxNumAttributes,
        maxNumEvents,
        maxNumLinks,
        maxNumAttributesPerEvent,
        maxNumAttributesPerLink,
        maxAttributeLength);
  }

  /**
   * Returns the global default max number of attributes per {@link Span}.
   *
   * @return the global default max number of attributes per {@link Span}.
   */
  public abstract int getMaxNumberOfAttributes();

  /**
   * Returns the global default max number of events per {@link Span}.
   *
   * @return the global default max number of events per {@code Span}.
   */
  public abstract int getMaxNumberOfEvents();

  /**
   * Returns the global default max number of links per {@link Span}.
   *
   * @return the global default max number of links per {@code Span}.
   */
  public abstract int getMaxNumberOfLinks();

  /**
   * Returns the global default max number of attributes per event.
   *
   * @return the global default max number of attributes per event.
   */
  public abstract int getMaxNumberOfAttributesPerEvent();

  /**
   * Returns the global default max number of attributes per link.
   *
   * @return the global default max number of attributes per link.
   */
  public abstract int getMaxNumberOfAttributesPerLink();

  /**
   * Returns the global default max length of string attribute value in characters.
   *
   * @return the global default max length of string attribute value in characters.
   * @see #shouldTruncateStringAttributeValues()
   */
  public abstract int getMaxLengthOfAttributeValues();

  public boolean shouldTruncateStringAttributeValues() {
    return getMaxLengthOfAttributeValues() != UNLIMITED_ATTRIBUTE_LENGTH;
  }

  /**
   * Returns a {@link TraceConfigBuilder} initialized to the same property values as the current
   * instance.
   *
   * @return a {@link TraceConfigBuilder} initialized to the same property values as the current
   *     instance.
   */
  public TraceConfigBuilder toBuilder() {
    return new TraceConfigBuilder()
        .setMaxNumberOfAttributes(getMaxNumberOfAttributes())
        .setMaxNumberOfEvents(getMaxNumberOfEvents())
        .setMaxNumberOfLinks(getMaxNumberOfLinks())
        .setMaxNumberOfAttributesPerEvent(getMaxNumberOfAttributesPerEvent())
        .setMaxNumberOfAttributesPerLink(getMaxNumberOfAttributesPerLink())
        .setMaxLengthOfAttributeValues(getMaxLengthOfAttributeValues());
  }
}
