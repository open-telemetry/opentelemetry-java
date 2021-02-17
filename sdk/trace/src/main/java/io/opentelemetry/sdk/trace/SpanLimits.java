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
      int maxNumAttributes,
      int maxNumEvents,
      int maxNumLinks,
      int maxNumAttributesPerEvent,
      int maxNumAttributesPerLink) {
    return new AutoValue_SpanLimits(
        maxNumAttributes,
        maxNumEvents,
        maxNumLinks,
        maxNumAttributesPerEvent,
        maxNumAttributesPerLink);
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
        .setMaxNumberOfAttributesPerLink(getMaxNumberOfAttributesPerLink());
  }
}
