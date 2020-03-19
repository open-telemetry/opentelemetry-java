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

package io.opentelemetry.sdk.trace;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.trace.Event;
import java.util.Collections;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/** Timed event. */
@Immutable
abstract class TimedEvent {

  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES =
      Collections.unmodifiableMap(Collections.<String, AttributeValue>emptyMap());
  private static final int DEFAULT_TOTAL_ATTRIBUTE_COUNT = 0;

  abstract long getEpochNanos();

  abstract String getName();

  abstract Map<String, AttributeValue> getAttributes();

  abstract int getTotalAttributeCount();

  TimedEvent() {}

  /**
   * Creates an {@link TimedEvent} with the given time, name and empty attributes.
   *
   * @param epochNanos epoch timestamp in nanos.
   * @param name the name of this {@code TimedEvent}.
   * @return an {@code TimedEvent}.
   */
  static TimedEvent create(long epochNanos, String name) {
    return create(epochNanos, name, EMPTY_ATTRIBUTES);
  }

  /**
   * Creates an {@link TimedEvent} with the given time, name and attributes.
   *
   * @param epochNanos epoch timestamp in nanos.
   * @param name the name of this {@code TimedEvent}.
   * @param attributes the attributes of this {@code TimedEvent}.
   * @return an {@code TimedEvent}.
   */
  static TimedEvent create(long epochNanos, String name, Map<String, AttributeValue> attributes) {
    return new AutoValue_TimedEvent_RawTimedEvent(epochNanos, name, attributes, attributes.size());
  }

  /**
   * Creates an {@link TimedEvent} with the given time, name and attributes.
   *
   * @param epochNanos epoch timestamp in nanos.
   * @param name the name of this {@code TimedEvent}.
   * @param attributes the attributes of this {@code TimedEvent}.
   * @return an {@code TimedEvent}.
   */
  static TimedEvent create(
      long epochNanos,
      String name,
      Map<String, AttributeValue> attributes,
      int totalAttributeCount) {
    return new AutoValue_TimedEvent_RawTimedEvent(
        epochNanos, name, attributes, totalAttributeCount);
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
    String getName() {
      return getEvent().getName();
    }

    @Override
    Map<String, AttributeValue> getAttributes() {
      return getEvent().getAttributes();
    }

    @Override
    abstract int getTotalAttributeCount();
  }

  @AutoValue
  @Immutable
  abstract static class RawTimedEvent extends TimedEvent {}
}
