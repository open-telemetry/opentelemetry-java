/*
 * Copyright 2019, OpenConsensus Authors
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

package openconsensus.trace.data;

import com.google.auto.value.AutoValue;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.concurrent.Immutable;
import openconsensus.internal.Utils;
import openconsensus.trace.AttributeValue;

/**
 * A text annotation with a set of attributes.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Event {
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES =
      Collections.unmodifiableMap(Collections.<String, AttributeValue>emptyMap());

  /**
   * Returns a new {@code Event} with the given name.
   *
   * @param name the text name of the {@code Event}.
   * @return a new {@code Event} with the given name.
   * @throws NullPointerException if {@code name} is {@code null}.
   * @since 0.1.0
   */
  public static Event fromName(String name) {
    return new AutoValue_Event(name, EMPTY_ATTRIBUTES);
  }

  /**
   * Returns a new {@code Event} with the given name and set of attributes.
   *
   * @param name the text name of the {@code Event}.
   * @param attributes the attributes of the {@code Event}.
   * @return a new {@code Event} with the given name and set of attributes.
   * @throws NullPointerException if {@code name} or {@code attributes} are {@code null}.
   * @since 0.1.0
   */
  public static Event fromNameAndAttributes(String name, Map<String, AttributeValue> attributes) {
    return new AutoValue_Event(
        name,
        Collections.unmodifiableMap(
            new HashMap<String, AttributeValue>(Utils.checkNotNull(attributes, "attributes"))));
  }

  /**
   * Return the name of the {@code Event}.
   *
   * @return the name of the {@code Event}.
   * @since 0.1.0
   */
  public abstract String getName();

  /**
   * Return the attributes of the {@code Event}.
   *
   * @return the attributes of the {@code Event}.
   * @since 0.1.0
   */
  public abstract Map<String, AttributeValue> getAttributes();

  Event() {}
}
