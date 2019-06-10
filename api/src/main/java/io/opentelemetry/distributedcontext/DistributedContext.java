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

import io.opentelemetry.context.Scope;
import java.util.Iterator;
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
   * Returns an iterator over the entries in this {@code DistributedContext}.
   *
   * @return an iterator over the entries in this {@code DistributedContext}.
   * @since 0.1.0
   */
  Iterator<Entry> getIterator();

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
     * Sets the parent {@code DistributedContext} to use. If not set, the value of {@code
     * Entryger.getCurrentDistributedContext()} at {@link #build()} or {@link #buildScoped()} time
     * will be used as parent.
     *
     * <p>This <b>must</b> be used to create a {@code DistributedContext} when manual Context
     * propagation is used.
     *
     * <p>If called multiple times, only the last specified value will be used.
     *
     * @param parent the {@code DistributedContext} used as parent.
     * @return this.
     * @throws NullPointerException if {@code parent} is {@code null}.
     * @see #setNoParent()
     * @since 0.1.0
     */
    Builder setParent(DistributedContext parent);

    /**
     * Sets the option to become a {@code DistributedContext} with no parent. If not set, the value
     * of {@code Entryger.getCurrentDistributedContext()} at {@link #build()} or {@link
     * #buildScoped()} time will be used as parent.
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

    /**
     * Enters the scope of code where the {@link DistributedContext} created from this builder is in
     * the current context and returns an object that represents that scope. The scope is exited
     * when the returned object is closed.
     *
     * @return an object that defines a scope where the {@code DistributedContext} created from this
     *     builder is set to the current context.
     * @since 0.1.0
     */
    Scope buildScoped();
  }
}
