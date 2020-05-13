/*
 * Copyright 2020, OpenTelemetry Authors
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

package io.opentelemetry.sdk.trace.data;

import com.google.auto.value.AutoValue;
import io.opentelemetry.common.AttributeValue;
import io.opentelemetry.sdk.trace.config.TraceConfig;
import io.opentelemetry.trace.Event;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * An {@link Event} with the associated time it took place.
 *
 * @since 0.1.0 TODO: This could be replaced with a non-lazy subclass of the
 *     io.opentelemetry.sdk.trace.TimedEvent
 */
@Immutable
@AutoValue
public abstract class TimedEvent implements Event {

  /**
   * Returns a new immutable {@code TimedEvent}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new immutable {@code TimedEvent<T>}
   * @since 0.1.0
   */
  public static TimedEvent create(
      long epochNanos, String name, Map<String, AttributeValue> attributes) {
    return new AutoValue_TimedEvent(epochNanos, name, attributes, attributes.size());
  }

  /**
   * Returns a new immutable {@code TimedEvent}.
   *
   * @param epochNanos epoch timestamp in nanos of the {@code Event}.
   * @param name the name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @param totalAttributeCount the total number of attributes for this {@code} Event.
   * @return a new immutable {@code TimedEvent<T>}
   * @since 0.1.0
   */
  public static TimedEvent create(
      long epochNanos,
      String name,
      Map<String, AttributeValue> attributes,
      int totalAttributeCount) {
    return new AutoValue_TimedEvent(epochNanos, name, attributes, totalAttributeCount);
  }

  /**
   * Returns the epoch time in nanos of this event.
   *
   * @return the epoch time in nanos of this event.
   * @since 0.1.0
   */
  public abstract long getEpochNanos();

  @Override
  public abstract String getName();

  @Override
  public abstract Map<String, AttributeValue> getAttributes();

  /**
   * The total number of attributes that were recorded on this Event. This number may be larger than
   * the number of attributes that are attached to this span, if the total number recorded was
   * greater than the configured maximum value. See: {@link
   * TraceConfig#getMaxNumberOfAttributesPerEvent()}
   *
   * @return The total number of attributes on this event.
   */
  public abstract int getTotalAttributeCount();

  TimedEvent() {}
}
