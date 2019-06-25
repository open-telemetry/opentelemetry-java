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

package io.opentelemetry.distributedcontext;

import java.util.Collection;
import javax.annotation.Nullable;
import javax.annotation.concurrent.Immutable;

/**
 * A map from {@link EntryKey} to {@link EntryValue} and {@link EntryMetadata} that can be used to
 * label anything that is associated with a specific operation.
 *
 * <p>For example, {@code DistributedContext}s can be used to label stats, log messages, or
 * debugging information.
 *
 * @since 0.1.0
 */
@Immutable
public interface DistributedContext {
  /**
   * Returns an immutable collection of the entries in this {@code DistributedContext}. Order of
   * entries is not guaranteed.
   *
   * @return an immutable collection of the entries in this {@code DistributedContext}.
   * @since 0.1.0
   */
  Collection<Entry> getEntries();

  /**
   * Returns the {@code EntryValue} associated with the given {@code EntryKey}.
   *
   * @param entryKey entry key to return the value for.
   * @return the {@code EntryValue} associated with the given {@code EntryKey}, or {@code null} if
   *     no {@code Entry} with the given {@code entryKey} is in this {@code DistributedContext}.
   */
  @Nullable
  EntryValue getEntryValue(EntryKey entryKey);

  /**
   * Builder for the {@link DistributedContext} class.
   *
   * @since 0.1.0
   */
  interface Builder {
    /**
     * Sets the parent {@link DistributedContext} to use. If not set, the value of {@link
     * DistributedContextManager#getCurrentContext()} at {@link #build()} time will be used as
     * parent.
     *
     * <p>This <b>must</b> be used to create a {@link DistributedContext} when manual Context
     * propagation is used.
     *
     * <p>If called multiple times, only the last specified value will be used.
     *
     * @param parent the {@link DistributedContext} used as parent.
     * @return this.
     * @throws NullPointerException if {@code parent} is {@code null}.
     * @see #setNoParent()
     * @since 0.1.0
     */
    Builder setParent(DistributedContext parent);

    /**
     * Sets the option to become a {@link DistributedContext} with no parent. If not set, the value
     * of {@link DistributedContextManager#getCurrentContext()} at {@link #build()} time will be
     * used as parent.
     *
     * @return this.
     * @since 0.1.0
     */
    Builder setNoParent();

    /**
     * Adds the key/value pair and metadata regardless of whether the key is present.
     *
     * @param key the {@code EntryKey} which will be set.
     * @param value the {@code EntryValue} to set for the given key.
     * @param entryMetadata the {@code EntryMetadata} associated with this {@link Entry}.
     * @return this
     * @since 0.1.0
     */
    Builder put(EntryKey key, EntryValue value, EntryMetadata entryMetadata);

    /**
     * Removes the key if it exists.
     *
     * @param key the {@code EntryKey} which will be removed.
     * @return this
     * @since 0.1.0
     */
    Builder remove(EntryKey key);

    /**
     * Creates a {@code DistributedContext} from this builder.
     *
     * @return a {@code DistributedContext} with the same entries as this builder.
     * @since 0.1.0
     */
    DistributedContext build();
  }
}
