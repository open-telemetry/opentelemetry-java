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

import openconsensus.tags.Tag;
import openconsensus.tags.TagContext;
import openconsensus.tags.TagMetadata;
import openconsensus.tags.TagMetadata.TagTtl;

/**
 * Object for serializing and deserializing {@link TagContext}s with the binary format.
 *
 * <p>See <a
 * href="https://github.com/census-instrumentation/opencensus-specs/blob/master/encodings/BinaryEncoding.md#tag-context">opencensus-specs</a>
 * for the specification of the cross-language binary serialization format.
 *
 * @since 0.1.0
 */
public abstract class TagContextBinarySerializer {

  /**
   * Serializes the {@code TagContext} into the on-the-wire representation.
   *
   * <p>This method should be the inverse of {@link #fromByteArray}.
   *
   * <p>{@link Tag}s that have a {@link TagMetadata} with {@link TagTtl#NO_PROPAGATION} will not be
   * serialized.
   *
   * @param tags the {@code TagContext} to serialize.
   * @return the on-the-wire representation of a {@code TagContext}.
   * @throws TagContextSerializationException if the result would be larger than the maximum allowed
   *     serialized size.
   * @since 0.1.0
   */
  public abstract byte[] toByteArray(TagContext tags) throws TagContextSerializationException;

  /**
   * Creates a {@code TagContext} from the given on-the-wire encoded representation.
   *
   * <p>This method should be the inverse of {@link #toByteArray}.
   *
   * @param bytes on-the-wire representation of a {@code TagContext}.
   * @return a {@code TagContext} deserialized from {@code bytes}.
   * @throws TagContextDeserializationException if there is a parse error, the input contains
   *     invalid tags, or the input is larger than the maximum allowed serialized size.
   * @since 0.1.0
   */
  public abstract TagContext fromByteArray(byte[] bytes) throws TagContextDeserializationException;
}
