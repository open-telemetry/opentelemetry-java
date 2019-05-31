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

package io.opentelemetry.tags;

import io.opentelemetry.context.Scope;
import java.util.Iterator;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link TagKey} to {@link TagValue} and {@link TagMetadata} that can be used to label
 * anything that is associated with a specific operation.
 *
 * <p>For example, {@code TagMap}s can be used to label stats, log messages, or debugging
 * information.
 *
 * @since 0.1.0
 */
@Immutable
public interface TagMap {
  /**
   * Returns an iterator over the tags in this {@code TagMap}.
   *
   * @return an iterator over the tags in this {@code TagMap}.
   * @since 0.1.0
   */
  Iterator<Tag> getIterator();

  /**
   * Returns the {@code TagValue} associated with the given {@code TagKey}.
   *
   * @param tagKey tag key to return the value for.
   * @return the {@code TagValue} associated with the given {@code TagKey}, or {@code null} if no
   *     {@code Tag} with the given {@code tagKey} is in this {@code TagMap}.
   */
  @Nullable
  TagValue getTagValue(TagKey tagKey);

  /**
   * Builder for the {@link TagMap} class.
   *
   * @since 0.1.0
   */
  interface Builder {
    /**
     * Sets the parent {@code TagMap} to use. If not set, the value of {@code
     * Tagger.getCurrentTagMap()} at {@link #build()} or {@link #buildScoped()} time will be used as
     * parent.
     *
     * <p>This <b>must</b> be used to create a {@code TagMap} when manual Context propagation is
     * used.
     *
     * <p>If called multiple times, only the last specified value will be used.
     *
     * @param parent the {@code TagMap} used as parent.
     * @return this.
     * @throws NullPointerException if {@code parent} is {@code null}.
     * @see #setNoParent()
     * @since 0.1.0
     */
    Builder setParent(TagMap parent);

    /**
     * Sets the option to become a {@code TagMap} with no parent. If not set, the value of {@code
     * Tagger.getCurrentTagMap()} at {@link #build()} or {@link #buildScoped()} time will be used as
     * parent.
     *
     * @return this.
     * @since 0.1.0
     */
    Builder setNoParent();

    /**
     * Adds the key/value pair and metadata regardless of whether the key is present.
     *
     * @param key the {@code TagKey} which will be set.
     * @param value the {@code TagValue} to set for the given key.
     * @param tagMetadata the {@code TagMetadata} associated with this {@link Tag}.
     * @return this
     * @since 0.1.0
     */
    Builder put(TagKey key, TagValue value, TagMetadata tagMetadata);

    /**
     * Removes the key if it exists on the builder. Tags inherited from a parent can't be removed.
     *
     * @param key the {@code TagKey} which will be removed.
     * @return this
     * @since 0.1.0
     */
    Builder remove(TagKey key);

    /**
     * Creates a {@code TagMap} from this builder.
     *
     * @return a {@code TagMap} with the same tags as this builder.
     * @since 0.1.0
     */
    TagMap build();

    /**
     * Enters the scope of code where the {@link TagMap} created from this builder is in the current
     * context and returns an object that represents that scope. The scope is exited when the
     * returned object is closed.
     *
     * @return an object that defines a scope where the {@code TagMap} created from this builder is
     *     set to the current context.
     * @since 0.1.0
     */
    Scope buildScoped();
  }
}
