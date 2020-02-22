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

package io.opentelemetry.correlationcontext;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * {@link EntryMetadata} contains properties associated with an {@link Entry}.
 *
 * <p>For now only the property {@link EntryTtl} is defined. In future, additional properties may be
 * added to address specific situations.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class EntryMetadata {

  EntryMetadata() {}

  /**
   * Creates an {@link EntryMetadata} with the given {@link EntryTtl}.
   *
   * @param entryTtl TTL of an {@code Entry}.
   * @return an {@code EntryMetadata}.
   * @since 0.1.0
   */
  public static EntryMetadata create(EntryTtl entryTtl) {
    return new AutoValue_EntryMetadata(entryTtl);
  }

  /**
   * Returns the {@link EntryTtl} of this {@link EntryMetadata}.
   *
   * @return the {@code EntryTtl}.
   * @since 0.1.0
   */
  public abstract EntryTtl getEntryTtl();

  /**
   * {@link EntryTtl} is an integer that represents number of hops an entry can propagate.
   *
   * <p>Anytime a sender serializes a entry, sends it over the wire and receiver deserializes the
   * entry then the entry is considered to have travelled one hop.
   *
   * <p>There could be one or more proxy(ies) between sender and receiver. Proxies are treated as
   * transparent entities and they are not counted as hops.
   *
   * <p>For now, only special values of {@link EntryTtl} are supported.
   *
   * @since 0.1.0
   */
  public enum EntryTtl {

    /**
     * An {@link Entry} with {@link EntryTtl#NO_PROPAGATION} is considered to have local scope and
     * is used within the process where it's created.
     *
     * @since 0.1.0
     */
    NO_PROPAGATION(0),

    /**
     * An {@link Entry} with {@link EntryTtl#UNLIMITED_PROPAGATION} can propagate unlimited hops.
     *
     * <p>However, it is still subject to outgoing and incoming (on remote side) filter criteria.
     *
     * <p>{@link EntryTtl#UNLIMITED_PROPAGATION} is typical used to track a request, which may be
     * processed across multiple entities.
     *
     * @since 0.1.0
     */
    UNLIMITED_PROPAGATION(-1);

    private final int hops;

    EntryTtl(int hops) {
      this.hops = hops;
    }

    int getHops() {
      return hops;
    }
  }
}
