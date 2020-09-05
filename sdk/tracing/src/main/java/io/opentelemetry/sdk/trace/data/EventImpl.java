/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.Attributes;
import javax.annotation.concurrent.Immutable;

/**
 * An immutable timed event representation. Enhances the core {@link io.opentelemetry.trace.Event}
 * by adding the time at which the event occurred.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class EventImpl implements SpanData.Event {

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   * @since 0.1.0
   */
  public static EventImpl create(long epochNanos, String name, Attributes attributes) {
    return new AutoValue_EventImpl(name, attributes, epochNanos, attributes.size());
  }

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @param totalAttributeCount the total number of attributes for this {@code} Event.
   * @return a new immutable {@code Event<T>}
   * @since 0.1.0
   */
  public static EventImpl create(
      long epochNanos, String name, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_EventImpl(name, attributes, epochNanos, totalAttributeCount);
  }

  EventImpl() {}
}
