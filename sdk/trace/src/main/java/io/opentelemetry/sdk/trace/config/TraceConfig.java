/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.config;

import com.google.auto.value.AutoValue;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import java.util.Map;
import java.util.Properties;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds global trace parameters.
 *
 * <p>Note: To update the TraceConfig associated with a {@link
 * io.opentelemetry.sdk.trace.TracerSdkManagement}, you should use the {@link #toBuilder()} method
 * on the TraceConfig currently assigned to the provider, make the changes desired to the {@link
 * Builder} instance, then use the {@link
 * io.opentelemetry.sdk.trace.TracerSdkManagement#updateActiveTraceConfig(TraceConfig)} with the
 * resulting TraceConfig instance.
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
  // These values are the default values for all the global parameters.
  // TODO: decide which default sampler to use
  private static final Sampler DEFAULT_SAMPLER = Sampler.parentBased(Sampler.alwaysOn());
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 1000;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 32;

  public static final int UNLIMITED_ATTRIBUTE_LENGTH = -1;
  private static final int DEFAULT_MAX_ATTRIBUTE_LENGTH = UNLIMITED_ATTRIBUTE_LENGTH;

  /**
   * Returns the default {@code TraceConfig}.
   *
   * @return the default {@code TraceConfig}.
   */
  public static TraceConfig getDefault() {
    return DEFAULT;
  }

  private static final TraceConfig DEFAULT = TraceConfig.builder().build();

  /**
   * Returns the global default {@code Sampler} which is used when constructing a new {@code Span}.
   *
   * @return the global default {@code Sampler}.
   */
  public abstract Sampler getSampler();

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
   * Returns a new {@link Builder}.
   *
   * @return a new {@link Builder}.
   */
  private static Builder builder() {
    return new AutoValue_TraceConfig.Builder()
        .setSampler(DEFAULT_SAMPLER)
        .setMaxNumberOfAttributes(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES)
        .setMaxNumberOfEvents(DEFAULT_SPAN_MAX_NUM_EVENTS)
        .setMaxNumberOfLinks(DEFAULT_SPAN_MAX_NUM_LINKS)
        .setMaxNumberOfAttributesPerEvent(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT)
        .setMaxNumberOfAttributesPerLink(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK)
        .setMaxLengthOfAttributeValues(DEFAULT_MAX_ATTRIBUTE_LENGTH);
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   */
  public abstract Builder toBuilder();

  /** Builder for {@link TraceConfig}. */
  @AutoValue.Builder
  public abstract static class Builder extends ConfigBuilder<Builder> {
    private static final String KEY_SAMPLER_PROBABILITY = "otel.config.sampler.probability";
    private static final String KEY_SPAN_ATTRIBUTE_COUNT_LIMIT = "otel.span.attribute.count.limit";
    private static final String KEY_SPAN_EVENT_COUNT_LIMIT = "otel.span.event.count.limit";
    private static final String KEY_SPAN_LINK_COUNT_LIMIT = "otel.span.link.count.limit";
    private static final String KEY_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT =
        "otel.config.max.event.attrs";
    private static final String KEY_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = "otel.config.max.link.attrs";
    private static final String KEY_SPAN_ATTRIBUTE_MAX_VALUE_LENGTH = "otel.config.max.attr.length";

    Builder() {}

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this
     */
    // Visible for testing
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, Builder.NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Double doubleValue = getDoubleProperty(KEY_SAMPLER_PROBABILITY, configMap);
      if (doubleValue != null) {
        this.setTraceIdRatioBased(doubleValue);
      }
      Integer intValue = getIntProperty(KEY_SPAN_ATTRIBUTE_COUNT_LIMIT, configMap);
      if (intValue != null) {
        this.setMaxNumberOfAttributes(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_EVENT_COUNT_LIMIT, configMap);
      if (intValue != null) {
        this.setMaxNumberOfEvents(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_LINK_COUNT_LIMIT, configMap);
      if (intValue != null) {
        this.setMaxNumberOfLinks(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT, configMap);
      if (intValue != null) {
        this.setMaxNumberOfAttributesPerEvent(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK, configMap);
      if (intValue != null) {
        this.setMaxNumberOfAttributesPerLink(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_ATTRIBUTE_MAX_VALUE_LENGTH, configMap);
      if (intValue != null) {
        this.setMaxLengthOfAttributeValues(intValue);
      }
      return this;
    }

    /**
     * * Sets the configuration values from the given properties object for only the available keys.
     *
     * @param properties {@link Properties} holding the configuration values.
     * @return this
     */
    @Override
    public Builder readProperties(Properties properties) {
      return super.readProperties(properties);
    }

    /**
     * * Sets the configuration values from environment variables for only the available keys.
     *
     * @return this.
     */
    @Override
    public Builder readEnvironmentVariables() {
      return super.readEnvironmentVariables();
    }

    /**
     * * Sets the configuration values from system properties for only the available keys.
     *
     * @return this.
     */
    @Override
    public Builder readSystemProperties() {
      return super.readSystemProperties();
    }

    /**
     * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link
     * #build()} will throw an exception.
     *
     * @param sampler the global default {@code Sampler}.
     * @return this.
     */
    public abstract Builder setSampler(Sampler sampler);

    /**
     * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link
     * #build()} will throw an exception.
     *
     * @param samplerRatio the global default ratio used to make decisions on {@link Span} sampling.
     * @return this.
     */
    public Builder setTraceIdRatioBased(double samplerRatio) {
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
    public abstract Builder setMaxNumberOfAttributes(int maxNumberOfAttributes);

    /**
     * Sets the global default max number of events per {@link Span}.
     *
     * @param maxNumberOfEvents the global default max number of events per {@link Span}. It must be
     *     positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfEvents(int maxNumberOfEvents);

    /**
     * Sets the global default max number of links per {@link Span}.
     *
     * @param maxNumberOfLinks the global default max number of links per {@link Span}. It must be
     *     positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfLinks(int maxNumberOfLinks);

    /**
     * Sets the global default max number of attributes per event.
     *
     * @param maxNumberOfAttributesPerEvent the global default max number of attributes per event.
     *     It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent);

    /**
     * Sets the global default max number of attributes per link.
     *
     * @param maxNumberOfAttributesPerLink the global default max number of attributes per link. It
     *     must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink);

    /**
     * Sets the global default max length of string attribute value in characters.
     *
     * @param maxLengthOfAttributeValues the global default max length of string attribute value in
     *     characters. It must be non-negative (or {@link #UNLIMITED_ATTRIBUTE_LENGTH}) otherwise
     *     {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxLengthOfAttributeValues(int maxLengthOfAttributeValues);

    abstract TraceConfig autoBuild();

    /**
     * Builds and returns a {@code TraceConfig} with the desired values.
     *
     * @return a {@code TraceConfig} with the desired values.
     * @throws IllegalArgumentException if any of the max numbers are not positive.
     */
    public TraceConfig build() {
      TraceConfig traceConfig = autoBuild();
      if (traceConfig.getMaxNumberOfAttributes() <= 0) {
        throw new IllegalArgumentException("maxNumberOfAttributes must be greater than 0");
      }
      if (traceConfig.getMaxNumberOfEvents() <= 0) {
        throw new IllegalArgumentException("maxNumberOfEvents must be greater than 0");
      }
      if (traceConfig.getMaxNumberOfLinks() <= 0) {
        throw new IllegalArgumentException("maxNumberOfLinks must be greater than 0");
      }
      if (traceConfig.getMaxNumberOfAttributesPerEvent() <= 0) {
        throw new IllegalArgumentException("maxNumberOfAttributesPerEvent must be greater than 0");
      }
      if (traceConfig.getMaxNumberOfAttributesPerLink() <= 0) {
        throw new IllegalArgumentException("maxNumberOfAttributesPerLink must be greater than 0");
      }
      if (traceConfig.getMaxLengthOfAttributeValues() <= 0
          && traceConfig.getMaxLengthOfAttributeValues() != UNLIMITED_ATTRIBUTE_LENGTH) {
        throw new IllegalArgumentException(
            "maxLengthOfAttributeValues must be -1 to "
                + "disable length restriction, or positive to enable length restriction");
      }
      return traceConfig;
    }
  }
}
