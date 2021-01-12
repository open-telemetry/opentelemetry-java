/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace.data;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.trace.config.TraceConfig;

public interface EventData {

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new immutable {@code Event<T>}
   */
  static EventData create(long epochNanos, String name, Attributes attributes) {
    return ImmutableEventData.create(epochNanos, name, attributes);
  }

  /**
   * Returns a new immutable {@code Event}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @param totalAttributeCount the total number of attributes for this {@code} Event.
   * @return a new immutable {@code Event<T>}
   */
  static EventData create(
      long epochNanos, String name, Attributes attributes, int totalAttributeCount) {
    return ImmutableEventData.create(epochNanos, name, attributes, totalAttributeCount);
  }

  /**
   * Return the name of the {@code Event}.
   *
   * @return the name of the {@code Event}.
   */
  String getName();

  /**
   * Return the attributes of the {@code Event}.
   *
   * @return the attributes of the {@code Event}.
   */
  Attributes getAttributes();

  /**
   * Returns the epoch time in nanos of this event.
   *
   * @return the epoch time in nanos of this event.
   */
  long getEpochNanos();

  /**
   * The total number of attributes that were recorded on this Event. This number may be larger than
   * the number of attributes that are attached to this span, if the total number recorded was
   * greater than the configured maximum value. See: {@link
   * TraceConfig#getMaxNumberOfAttributesPerEvent()}
   *
   * @return The total number of attributes on this event.
   */
  int getTotalAttributeCount();

  /**
   * Returns the dropped attributes count of this event.
   *
   * @return the dropped attributes count of this event.
   */
  default int getDroppedAttributesCount() {
    return getTotalAttributeCount() - getAttributes().size();
  }
}
