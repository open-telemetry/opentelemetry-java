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

/**
 * A text annotation with a set of attributes.
 *
 * @since 0.1
 */
@Immutable
@AutoValue
public abstract class Annotation {
  private static final Map<String, AttributeValue> EMPTY_ATTRIBUTES =
      Collections.unmodifiableMap(Collections.<String, AttributeValue>emptyMap());

  /**
   * Returns a new {@code Annotation} with the given description.
   *
   * @param description the text description of the {@code Annotation}.
   * @return a new {@code Annotation} with the given description.
   * @throws NullPointerException if {@code description} is {@code null}.
   * @since 0.1
   */
  public static Annotation fromDescription(String description) {
    return new AutoValue_Annotation(description, EMPTY_ATTRIBUTES);
  }

  /**
   * Returns a new {@code Annotation} with the given description and set of attributes.
   *
   * @param description the text description of the {@code Annotation}.
   * @param attributes the attributes of the {@code Annotation}.
   * @return a new {@code Annotation} with the given description and set of attributes.
   * @throws NullPointerException if {@code description} or {@code attributes} are {@code null}.
   * @since 0.1
   */
  public static Annotation fromDescriptionAndAttributes(
      String description, Map<String, AttributeValue> attributes) {
    return new AutoValue_Annotation(
        description,
        Collections.unmodifiableMap(
            new HashMap<String, AttributeValue>(Utils.checkNotNull(attributes, "attributes"))));
  }

  /**
   * Return the description of the {@code Annotation}.
   *
   * @return the description of the {@code Annotation}.
   * @since 0.1
   */
  public abstract String getDescription();

  /**
   * Return the attributes of the {@code Annotation}.
   *
   * @return the attributes of the {@code Annotation}.
   * @since 0.1
   */
  public abstract Map<String, AttributeValue> getAttributes();

  Annotation() {}
}
