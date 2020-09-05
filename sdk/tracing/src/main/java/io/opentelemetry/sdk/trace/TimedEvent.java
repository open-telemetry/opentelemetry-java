/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.trace.Event;
import javax.annotation.concurrent.Immutable;

/** Timed event. */
@Immutable
abstract class TimedEvent implements io.opentelemetry.sdk.trace.data.SpanData.Event {

  private static final int DEFAULT_TOTAL_ATTRIBUTE_COUNT = 0;

  TimedEvent() {}

  /**
   * Creates an {@link TimedEvent} with the given time, name and attributes.
   *
   * @param epochNanos epoch timestamp in nanos.
   * @param name the name of this {@code TimedEvent}.
   * @param attributes the attributes of this {@code TimedEvent}.
   * @return an {@code TimedEvent}.
   */
  static TimedEvent create(
      long epochNanos, String name, Attributes attributes, int totalAttributeCount) {
    return new AutoValue_TimedEvent_RawTimedEvent(
        name, attributes, epochNanos, totalAttributeCount);
  }

  /**
   * Creates an {@link TimedEvent} with the given time and event.
   *
   * @param epochNanos epoch timestamp in nanos.
   * @param event the event.
   * @return an {@code TimedEvent}.
   */
  static TimedEvent create(long epochNanos, Event event) {
    return new AutoValue_TimedEvent_RawTimedEventWithEvent(
        epochNanos, event, DEFAULT_TOTAL_ATTRIBUTE_COUNT);
  }

  @AutoValue
  @Immutable
  abstract static class RawTimedEventWithEvent extends TimedEvent {
    abstract Event getEvent();

    @Override
    public String getName() {
      return getEvent().getName();
    }

    @Override
    public Attributes getAttributes() {
      return getEvent().getAttributes();
    }

    @Override
    public abstract int getTotalAttributeCount();
  }

  @AutoValue
  @Immutable
  abstract static class RawTimedEvent extends TimedEvent {}
}
