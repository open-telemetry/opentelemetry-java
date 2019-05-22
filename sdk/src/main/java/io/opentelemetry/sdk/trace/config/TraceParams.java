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
import io.opentelemetry.trace.Event;
import io.opentelemetry.trace.Link;
import io.opentelemetry.trace.Span;
import javax.annotation.concurrent.Immutable;

/** Class that holds global trace parameters. */
@AutoValue
@Immutable
public abstract class TraceParams {
  // These values are the default values for all the global parameters.
  private static final int DEFAULT_SPAN_MAX_NUM_ATTRIBUTES = 32;
  private static final int DEFAULT_SPAN_MAX_NUM_EVENTS = 128;
  private static final int DEFAULT_SPAN_MAX_NUM_LINKS = 32;

  /** Default {@code TraceParams}. */
  public static final TraceParams DEFAULT =
      TraceParams.builder()
          .setMaxNumberOfAttributes(DEFAULT_SPAN_MAX_NUM_ATTRIBUTES)
          .setMaxNumberOfEvents(DEFAULT_SPAN_MAX_NUM_EVENTS)
          .setMaxNumberOfLinks(DEFAULT_SPAN_MAX_NUM_LINKS)
          .build();

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

  private static Builder builder() {
    return new AutoValue_TraceParams.Builder();
  }

  /**
   * Returns a {@link Builder} initialized to the same property values as the current instance.
   *
   * @return a {@link Builder} initialized to the same property values as the current instance.
   */
  public abstract Builder toBuilder();

  /** A {@code Builder} class for {@link TraceParams}. */
  @AutoValue.Builder
  public abstract static class Builder {

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

    abstract TraceParams autoBuild();

    /**
     * Builds and returns a {@code TraceParams} with the desired values.
     *
     * @return a {@code TraceParams} with the desired values.
     * @throws IllegalArgumentException if any of the max numbers are not positive.
     */
    public TraceParams build() {
      TraceParams traceParams = autoBuild();
      Preconditions.checkArgument(
          traceParams.getMaxNumberOfAttributes() > 0, "maxNumberOfAttributes");
      Preconditions.checkArgument(traceParams.getMaxNumberOfEvents() > 0, "maxNumberOfEvents");
      Preconditions.checkArgument(traceParams.getMaxNumberOfLinks() > 0, "maxNumberOfLinks");
      return traceParams;
    }
  }
}
