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
import io.opentelemetry.trace.AttributeValue;
import io.opentelemetry.trace.Event;
import java.util.Collections;
import java.util.Map;
import javax.annotation.concurrent.Immutable;

/** Implementation of {@link Event}. */
@Immutable
@AutoValue
abstract class EventSdk implements Event {

  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES =
      Collections.unmodifiableMap(Collections.<String, AttributeValue>emptyMap());

  /**
   * Creates an {@link EventSdk} with the given name and empty attributes.
   *
   * @param name the name of this {@code EventSdk}.
   * @return an {@code EventSdk}.
   */
  public static EventSdk create(String name) {
    return new AutoValue_EventSdk(name, EMPTY_ATTRIBUTES);
  }

  /**
   * Creates an {@link EventSdk} with the given name and attributes.
   *
   * @param name the name of this {@code EventSdk}.
   * @param attributes the attributes of this {@code EventSdk}.
   * @return an {@code EventSdk}.
   */
  public static EventSdk create(String name, Map<String, AttributeValue> attributes) {
    return new AutoValue_EventSdk(name, attributes);
  }

  EventSdk() {}
}
