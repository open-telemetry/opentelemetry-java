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

package openconsensus.tags.propagation;

import openconsensus.tags.TagMap;
import openconsensus.tags.data.Tag;
import openconsensus.tags.data.TagMetadata;
import openconsensus.tags.data.TagMetadata.TagTtl;

/**
 * Object for serializing and deserializing {@link TagMap}s with the binary format.
 *
 * @since 0.1.0
 */
public abstract class TagMapBinarySerializer {

  /**
   * Serializes the {@code TagMap} into the on-the-wire representation.
   *
   * <p>This method should be the inverse of {@link #fromByteArray}.
   *
   * <p>{@link Tag}s that have a {@link TagMetadata} with {@link TagTtl#NO_PROPAGATION} will not be
   * serialized.
   *
   * @param tags the {@code TagMap} to serialize.
   * @return the on-the-wire representation of a {@code TagMap}.
   * @throws TagMapSerializationException if the result would be larger than the maximum allowed
   *     serialized size.
   * @since 0.1.0
   */
  public abstract byte[] toByteArray(TagMap tags) throws TagMapSerializationException;

  /**
   * Creates a {@code TagMap} from the given on-the-wire encoded representation.
   *
   * <p>This method should be the inverse of {@link #toByteArray}.
   *
   * @param bytes on-the-wire representation of a {@code TagMap}.
   * @return a {@code TagMap} deserialized from {@code bytes}.
   * @throws TagMapDeserializationException if there is a parse error, the input contains invalid
   *     tags, or the input is larger than the maximum allowed serialized size.
   * @since 0.1.0
   */
  public abstract TagMap fromByteArray(byte[] bytes) throws TagMapDeserializationException;
}
