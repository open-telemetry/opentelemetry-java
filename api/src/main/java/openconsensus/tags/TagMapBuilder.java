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

package openconsensus.tags;

import openconsensus.common.Scope;

/**
 * Builder for the {@link TagMap} class.
 *
 * @since 0.1.0
 */
public abstract class TagMapBuilder {
  /**
   * Adds the key/value pair and metadata regardless of whether the key is present.
   *
   * @param key the {@code TagKey} which will be set.
   * @param value the {@code TagValue} to set for the given key.
   * @param tagMetadata the {@code TagMetadata} associated with this {@link Tag}.
   * @return this
   * @since 0.1.0
   */
  public abstract TagMapBuilder put(TagKey key, TagValue value, TagMetadata tagMetadata);

  /**
   * Removes the key if it exists.
   *
   * @param key the {@code TagKey} which will be removed.
   * @return this
   * @since 0.1.0
   */
  public abstract TagMapBuilder remove(TagKey key);

  /**
   * Creates a {@code TagMap} from this builder.
   *
   * @return a {@code TagMap} with the same tags as this builder.
   * @since 0.1.0
   */
  public abstract TagMap build();

  /**
   * Enters the scope of code where the {@link TagMap} created from this builder is in the current
   * context and returns an object that represents that scope. The scope is exited when the returned
   * object is closed.
   *
   * @return an object that defines a scope where the {@code TagMap} created from this builder is
   *     set to the current context.
   * @since 0.1.0
   */
  public abstract Scope buildScoped();
}
