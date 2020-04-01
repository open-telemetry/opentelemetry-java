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

import com.google.auto.value.AutoValue;
import com.google.common.base.Preconditions;
import io.opentelemetry.sdk.trace.Sampler;
import io.opentelemetry.sdk.trace.Samplers;
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
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
@AutoValue
@Immutable
public abstract class TraceConfig {
  // These values are the default values for all the global parameters.
  // TODO: decide which default sampler to use
  private static final Sampler DEFAULT_SAMPLER = Samplers.alwaysOn();
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK = 32;

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
      TraceConfig.newBuilder()
          .setSampler(DEFAULT_SAMPLER)
          .setMaxNumberOfAttributes(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES)
          .setMaxNumberOfEvents(DEFAULT_SPAN_MAX_NUM_EVENTS)
          .setMaxNumberOfLinks(DEFAULT_SPAN_MAX_NUM_LINKS)
          .setMaxNumberOfAttributesPerEvent(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_EVENT)
          .setMaxNumberOfAttributesPerLink(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES_PER_LINK)
          .build();

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
   * Returns the global default max number of {@link Event}s per {@link Span}.
   *
   * @return the global default max number of {@code Event}s per {@code Span}.
   */
  public abstract int getMaxNumberOfEvents();

  /**
   * Returns the global default max number of {@link Link} entries per {@link Span}.
   *
   * @return the global default max number of {@code Link} entries per {@code Span}.
   */
  public abstract int getMaxNumberOfLinks();

  /**
   * Returns the global default max number of attributes per {@link Event}.
   *
   * @return the global default max number of attributes per {@link Event}.
   */
  public abstract int getMaxNumberOfAttributesPerEvent();

  /**
   * Returns the global default max number of attributes per {@link Link}.
   *
   * @return the global default max number of attributes per {@link Link}.
   */
  public abstract int getMaxNumberOfAttributesPerLink();

  /**
   * Returns a new {@link Builder}.
   *
   * @return a new {@link Builder}.
   */
  private static Builder newBuilder() {
    return new AutoValue_TraceConfig.Builder();
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   */
  public abstract Builder toBuilder();

  /** A {@code Builder} class for {@link TraceConfig}. */
  @AutoValue.Builder
  public abstract static class Builder {

    /**
     * Sets the global default {@code Sampler}. It must be not {@code null} otherwise {@link
     * #build()} will throw an exception.
     *
     * @param sampler the global default {@code Sampler}.
     * @return this.
     */
    public abstract Builder setSampler(Sampler sampler);

    /**
     * Sets the global default max number of attributes per {@link Span}.
     *
     * @param maxNumberOfAttributes the global default max number of attributes per {@link Span}. It
     *     must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfAttributes(int maxNumberOfAttributes);

    /**
     * Sets the global default max number of {@link Event}s per {@link Span}.
     *
     * @param maxNumberOfEvents the global default max number of {@link Event}s per {@link Span}. It
     *     must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfEvents(int maxNumberOfEvents);

    /**
     * Sets the global default max number of {@link Link} entries per {@link Span}.
     *
     * @param maxNumberOfLinks the global default max number of {@link Link} entries per {@link
     *     Span}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfLinks(int maxNumberOfLinks);

    /**
     * Sets the global default max number of attributes per {@link Event}.
     *
     * @param maxNumberOfAttributesPerEvent the global default max number of attributes per {@link
     *     Event}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfAttributesPerEvent(int maxNumberOfAttributesPerEvent);

    /**
     * Sets the global default max number of attributes per {@link Link}.
     *
     * @param maxNumberOfAttributesPerLink the global default max number of attributes per {@link
     *     Link}. It must be positive otherwise {@link #build()} will throw an exception.
     * @return this.
     */
    public abstract Builder setMaxNumberOfAttributesPerLink(int maxNumberOfAttributesPerLink);

    abstract TraceConfig autoBuild();

    /**
     * Builds and returns a {@code TraceConfig} with the desired values.
     *
     * @return a {@code TraceConfig} with the desired values.
     * @throws IllegalArgumentException if any of the max numbers are not positive.
     */
    public TraceConfig build() {
      TraceConfig traceConfig = autoBuild();
      Preconditions.checkArgument(
          traceConfig.getMaxNumberOfAttributes() > 0, "maxNumberOfAttributes");
      Preconditions.checkArgument(traceConfig.getMaxNumberOfEvents() > 0, "maxNumberOfEvents");
      Preconditions.checkArgument(traceConfig.getMaxNumberOfLinks() > 0, "maxNumberOfLinks");
      Preconditions.checkArgument(
          traceConfig.getMaxNumberOfAttributesPerEvent() > 0, "maxNumberOfAttributesPerEvent");
      Preconditions.checkArgument(
          traceConfig.getMaxNumberOfAttributesPerLink() > 0, "maxNumberOfAttributesPerLink");
      return traceConfig;
    }
  }
}
