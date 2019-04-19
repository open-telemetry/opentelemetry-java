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

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * {@link TagMetadata} contains properties associated with a {@link Tag}.
 *
 * <p>For now only the property {@link TagTtl} is defined. In future, additional properties may be
 * added to address specific situations.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class TagMetadata {

  TagMetadata() {}

  /**
   * Creates a {@link TagMetadata} with the given {@link TagTtl}.
   *
   * @param tagTtl TTL of a {@code Tag}.
   * @return a {@code TagMetadata}.
   * @since 0.1.0
   */
  public static TagMetadata create(TagTtl tagTtl) {
    return new AutoValue_TagMetadata(tagTtl);
  }

  /**
   * Returns the {@link TagTtl} of this {@link TagMetadata}.
   *
   * @return the {@code TagTtl}.
   * @since 0.1.0
   */
  public abstract TagTtl getTagTtl();

  /**
   * {@link TagTtl} is an integer that represents number of hops a tag can propagate.
   *
   * <p>Anytime a sender serializes a tag, sends it over the wire and receiver deserializes the tag
   * then the tag is considered to have travelled one hop.
   *
   * <p>There could be one or more proxy(ies) between sender and receiver. Proxies are treated as
   * transparent entities and they are not counted as hops.
   *
   * <p>For now, only special values of {@link TagTtl} are supported.
   *
   * @since 0.1.0
   */
  public enum TagTtl {

    /**
     * A {@link Tag} with {@link TagTtl#NO_PROPAGATION} is considered to have local scope and is
     * used within the process where it's created.
     *
     * @since 0.1.0
     */
    NO_PROPAGATION(0),

    /**
     * A {@link Tag} with {@link TagTtl#UNLIMITED_PROPAGATION} can propagate unlimited hops.
     *
     * <p>However, it is still subject to outgoing and incoming (on remote side) filter criteria.
     *
     * <p>{@link TagTtl#UNLIMITED_PROPAGATION} is typical used to track a request, which may be
     * processed across multiple entities.
     *
     * @since 0.1.0
     */
    UNLIMITED_PROPAGATION(-1);

    private final int hops;

    private TagTtl(int hops) {
      this.hops = hops;
    }

    int getHops() {
      return hops;
    }
  }
}
