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

package openconsensus.trace;

import java.util.Map;
import javax.annotation.concurrent.Immutable;

/**
 * A text annotation with a set of attributes.
 *
 * @since 0.1.0
 */
@Immutable
public abstract class Event {
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

  /** Protected constructor to allow subclassing this class. */
  protected Event() {}
}
