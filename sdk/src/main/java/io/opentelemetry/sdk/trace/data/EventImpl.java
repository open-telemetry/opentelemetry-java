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
