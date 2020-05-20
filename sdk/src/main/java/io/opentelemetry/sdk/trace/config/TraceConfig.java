/*
 * Copyright 2019, OpenTelemetry Authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.opentelemetry.sdk.trace.config;

import com.google.common.annotations.VisibleForTesting;
import io.opentelemetry.internal.Utils;
import io.opentelemetry.sdk.common.export.ConfigBuilder;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import java.util.Map;
import java.util.Properties;
import javax.annotation.concurrent.Immutable;

/**
 * Class that holds global trace parameters.
 *
 * <p>Note: To update the TraceConfig associated with a {@link
 * io.opentelemetry.sdk.trace.TracerSdkProvider}, you should use the {@link #toBuilder()} method on
 * the TraceConfig currently assigned to the provider, make the changes desired to the {@link
 * Builder} instance, then use the {@link
 * io.opentelemetry.sdk.trace.TracerSdkProvider#updateActiveTraceConfig(TraceConfig)} with the
 * resulting TraceConfig instance.
 */
@Immutable
public class TraceConfig {
  // These values are the default values for all the global parameters.
  // TODO: decide which default sampler to use
  private final Sampler sampler;
  private final int maxNumberOfAttributes;
  private final int maxNumberOfEvents;
  private final int maxNumberOfLinks;
  private final int maxNumberOfAttributesPerEvent;
  private final int maxNumberOfAttributesPerLink;

  /**
   * Returns the default {@code TraceConfig}.
   *
   * @return the default {@code TraceConfig}.
   * @since 0.1.0
   */
  public static TraceConfig getDefault() {
    return DEFAULT;
  }

  private static final TraceConfig DEFAULT =
      TraceConfig.newBuilder().readEnvironmentVariables().readSystemProperties().build();

  private TraceConfig(
      Sampler sampler,
      int maxNumberOfAttributes,
      int maxNumberOfEvents,
      int maxNumberOfLinks,
      int maxNumberOfAttributesPerEvent,
      int maxNumberOfAttributesPerLink) {
    this.sampler = sampler;
    this.maxNumberOfAttributes = maxNumberOfAttributes;
    this.maxNumberOfEvents = maxNumberOfEvents;
    this.maxNumberOfLinks = maxNumberOfLinks;
    this.maxNumberOfAttributesPerEvent = maxNumberOfAttributesPerEvent;
    this.maxNumberOfAttributesPerLink = maxNumberOfAttributesPerLink;
  }

  /**
   * Returns the global default {@code Sampler} which is used when constructing a new {@code Span}.
   *
   * @return the global default {@code Sampler}.
   */
  public Sampler getSampler() {
    return sampler;
  }

  /**
   * Returns the global default max number of attributes per {@link Span}.
   *
   * @return the global default max number of attributes per {@link Span}.
   */
  public int getMaxNumberOfAttributes() {
    return maxNumberOfAttributes;
  }

  /**
   * Returns the global default max number of {@link Event}s per {@link Span}.
   *
   * @return the global default max number of {@code Event}s per {@code Span}.
   */
  public int getMaxNumberOfEvents() {
    return maxNumberOfEvents;
  }

  /**
   * Returns the global default max number of {@link Link} entries per {@link Span}.
   *
   * @return the global default max number of {@code Link} entries per {@code Span}.
   */
  public int getMaxNumberOfLinks() {
    return maxNumberOfLinks;
  }

  /**
   * Returns the global default max number of attributes per {@link Event}.
   *
   * @return the global default max number of attributes per {@link Event}.
   */
  public int getMaxNumberOfAttributesPerEvent() {
    return maxNumberOfAttributesPerEvent;
  }

  /**
   * Returns the global default max number of attributes per {@link Link}.
   *
   * @return the global default max number of attributes per {@link Link}.
   */
  public int getMaxNumberOfAttributesPerLink() {
    return maxNumberOfAttributesPerLink;
  }

  /**
   * Returns a new {@link Builder}.
   *
   * @return a new {@link Builder}.
   */
  public static Builder newBuilder() {
    return new Builder();
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   */
  public Builder toBuilder() {
    return new Builder(this);
  }

  public static final class Builder extends ConfigBuilder<Builder> {
    private static final String KEY_SAMPLER_PROBABILITY = "otel.config.sampler.probability";
    private static final String KEY_SPAN_MAX_NUM_ATTRIBUTES = "otel.config.max.attrs";
    private static final String KEY_SPAN_MAX_NUM_EVENTS = "otel.config.max.events";
    private static final String KEY_SPAN_MAX_NUM_LINKS = "otel.config.max.links";
    private static final String KEY_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT =
        "otel.config.max.event.attrs";
    private static final String KEY_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = "otel.config.max.link.attrs";

    private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 32;
    private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 128;
    private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 32;
    private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 32;
    private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 32;

    private Sampler sampler = Samplers.alwaysOn();
    private int maxNumberOfAttributes = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES;
    private int maxNumberOfEvents = DEFAULT_SPAN_MAX_NUM_EVENTS;
    private int maxNumberOfLinks = DEFAULT_SPAN_MAX_NUM_LINKS;
    private int maxNumberOfAttributesPerEvent = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT;
    private int maxNumberOfAttributesPerLink = DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK;

    private Builder(TraceConfig traceConfig) {
      this.sampler = traceConfig.sampler;
      this.maxNumberOfEvents = traceConfig.maxNumberOfEvents;
      this.maxNumberOfLinks = traceConfig.maxNumberOfLinks;
      this.maxNumberOfAttributesPerLink = traceConfig.maxNumberOfAttributesPerLink;
      this.maxNumberOfAttributesPerEvent = traceConfig.maxNumberOfAttributesPerEvent;
    }

    private Builder() {}

    /**
     * Sets the configuration values from the given configuration map for only the available keys.
     * This method looks for the following keys:
     *
     * <ul>
     *   <li>{@code otel.config.sampler.probability}: to set the global default sampler for traces.
     *   <li>{@code otel.config.max.attrs}: to set the global default max number of attributes per
     *       {@link Span}.
     *   <li>{@code otel.config.max.events}: to set the global default max number of {@link Event}s
     *       per {@link Span}.
     *   <li>{@code otel.config.max.links}: to set the global default max number of {@link Link}
     *       entries per {@link Span}.
     *   <li>{@code otel.config.max.event.attrs}: to set the global default max number of attributes
     *       per {@link Event}.
     *   <li>{@code otel.config.max.link.attrs}: to set the global default max number of attributes
     *       per {@link Link}.
     * </ul>
     *
     * @param configMap {@link Map} holding the configuration values.
     * @return this.
     */
    @VisibleForTesting
    @Override
    protected Builder fromConfigMap(
        Map<String, String> configMap, Builder.NamingConvention namingConvention) {
      configMap = namingConvention.normalize(configMap);
      Double doubleValue = getDoubleProperty(KEY_SAMPLER_PROBABILITY, configMap);
      if (doubleValue != null) {
        this.setSamplerProbability(doubleValue);
      }
      Integer intValue = getIntProperty(KEY_SPAN_MAX_NUM_ATTRIBUTES, configMap);
      if (intValue != null) {
        this.setMaxNumberOfAttributes(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_MAX_NUM_EVENTS, configMap);
      if (intValue != null) {
        this.setMaxNumberOfEvents(intValue);
      }
      intValue = getIntProperty(KEY_SPAN_MAX_NUM_LINKS, configMap);
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
      return this;
    }

    /**
     * Sets the configuration values from the given properties object for only the available keys.
     * This method looks for the following keys:
     *
     * <ul>
     *   <li>{@code otel.config.sampler.probability}: to set the global default sampler for traces.
     *   <li>{@code otel.config.max.attrs}: to set the global default max number of attributes per
     *       {@link Span}.
     *   <li>{@code otel.config.max.events}: to set the global default max number of {@link Event}s
     *       per {@link Span}.
     *   <li>{@code otel.config.max.links}: to set the global default max number of {@link Link}
     *       entries per {@link Span}.
     *   <li>{@code otel.config.max.event.attrs}: to set the global default max number of attributes
     *       per {@link Event}.
     *   <li>{@code otel.config.max.link.attrs}: to set the global default max number of attributes
     *       per {@link Link}.
     * </ul>
     *
     * @param properties {@link Properties} holding the configuration values.
     * @return this.
     */
    @Override
    public Builder readProperties(Properties properties) {
      return super.readProperties(properties);
    }

    /**
     * Sets the configuration values from environment variables for only the available keys. This
     * method looks for the following keys:
     *
     * <ul>
     *   <li>{@code OTEL_CONFIG_SAMPLER_PROBABILITY}: to set the global default sampler for traces.
     *   <li>{@code OTEL_CONFIG_MAX_ATTRS}: to set the global default max number of attributes per
     *       {@link Span}.
     *   <li>{@code OTEL_CONFIG_MAX_EVENTS}: to set the global default max number of {@link Event}s
     *       per {@link Span}.
     *   <li>{@code OTEL_CONFIG_MAX_LINKS}: to set the global default max number of {@link Link}
     *       entries per {@link Span}.
     *   <li>{@code OTEL_CONFIG_MAX_EVENT_ATTRS}: to set the global default max number of attributes
     *       per {@link Event}.
     *   <li>{@code OTEL_CONFIG_MAX_LINK_ATTRS}: to set the global default max number of attributes
     *       per {@link Link}.
     * </ul>
     *
     * @return this.
     */
    @Override
    public Builder readEnvironmentVariables() {
      return super.readEnvironmentVariables();
    }

    /**
     * Sets the configuration values from system properties for only the available keys. This method
     * looks for the following keys:
     *
     * <ul>
     *   <li>{@code otel.config.sampler.probability}: to set the global default sampler for traces.
     *   <li>{@code otel.config.max.attrs}: to set the global default max number of attributes per
     *       {@link Span}.
     *   <li>{@code otel.config.max.events}: to set the global default max number of {@link Event}s
     *       per {@link Span}.
     *   <li>{@code otel.config.max.links}: to set the global default max number of {@link Link}
     *       entries per {@link Span}.
     *   <li>{@code otel.config.max.event.attrs}: to set the global default max number of attributes
     *       per {@link Event}.
     *   <li>{@code otel.config.max.link.attrs}: to set the global default max number of attributes
     *       per {@link Link}.
     * </ul>
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
    public Builder setSampler(Sampler sampler) {
      Utils.checkArgument(sampler != null, "sampler should not be null");
      this.sampler = sampler;
      return this;
    }

    /**
     * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link
     * #build()} will throw an exception.
     *
     * @param samplerProbability the global default {@code Sampler}.
     * @return this.
     */
    public Builder setSamplerProbability(double samplerProbability) {
      Utils.checkArgument(
          samplerProbability >= 0, "samplerProbability must be greater than or equal to 0.");
      Utils.checkArgument(
          samplerProbability <= 1, "samplerProbability must be lesser than or equal to 1.");
      if (samplerProbability == 1) {
        sampler = Samplers.alwaysOn();
      } else if (samplerProbability == 0) {
        sampler = Samplers.alwaysOff();
      } else {
        sampler = Samplers.probability(samplerProbability);
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
    public Builder setMaxNumberOfAttributes(int maxNumberOfAttributes) {
      Utils.checkArgument(maxNumberOfAttributes > 0, "maxNumberOfAttributes must be positive.");
      this.maxNumberOfAttributes = maxNumberOfAttributes;
      return this;
    }

    /**
     * Sets the global default max number of {@link Event}s per {@link Span}.
     *
     * @param maxNumberOfEvents the global default max number of {@link Event}s per {@link Span}. It
     *     must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public Builder setMaxNumberOfEvents(int maxNumberOfEvents) {
      Utils.checkArgument(maxNumberOfEvents > 0, "maxNumberOfEvents must be positive.");
      this.maxNumberOfEvents = maxNumberOfEvents;
      return this;
    }

    /**
     * Sets the global default max number of {@link Link} entries per {@link Span}.
     *
     * @param maxNumberOfLinks the global default max number of {@link Link} entries per {@link
     *     Span}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public Builder setMaxNumberOfLinks(int maxNumberOfLinks) {
      Utils.checkArgument(maxNumberOfLinks > 0, "maxNumberOfLinks must be positive.");
      this.maxNumberOfLinks = maxNumberOfLinks;
      return this;
    }

    /**
     * Sets the global default max number of attributes per {@link Event}.
     *
     * @param maxNumberOfAttributesPerEvent the global default max number of attributes per {@link
     *     Event}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public Builder setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent) {
      Utils.checkArgument(
          maxNumberOfAttributesPerEvent > 0, "maxNumberOfAttributesPerEvent must be positive.");
      this.maxNumberOfAttributesPerEvent = maxNumberOfAttributesPerEvent;
      return this;
    }

    /**
     * Sets the global default max number of attributes per {@link Link}.
     *
     * @param maxNumberOfAttributesPerLink the global default max number of attributes per {@link
     *     Link}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public Builder setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink) {
      Utils.checkArgument(
          maxNumberOfAttributesPerLink > 0, "maxNumberOfAttributesPerLink must be positive.");
      this.maxNumberOfAttributesPerLink = maxNumberOfAttributesPerLink;
      return this;
    }

    /**
     * Builds and returns a {@code TraceConfig} with the desired values.
     *
     * @return a {@code TraceConfig} with the desired values.
     * @throws IllegalArgumentException if any of the max numbers are not positive.
     */
    public TraceConfig build() {
      return new TraceConfig(
          sampler,
          maxNumberOfAttributes,
          maxNumberOfEvents,
          maxNumberOfLinks,
          maxNumberOfAttributesPerEvent,
          maxNumberOfAttributesPerLink);
    }
  }
}
