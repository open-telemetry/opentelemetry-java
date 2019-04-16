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

package openconsensus.opencensusshim.tags;

import com.google.auto.value.AutoValue;
import javax.annotation.concurrent.Immutable;

/**
 * {@link TagKey} paired with a {@link TagValue}.
 *
 * @since 0.1.0
 */
@Immutable
@AutoValue
public abstract class Tag {

  Tag() {}

  /**
   * Creates a {@code Tag} from the given key, value and metadata.
   *
   * @param key the tag key.
   * @param value the tag value.
   * @param tagMetadata the tag metadata.
   * @return a {@code Tag}.
   * @since 0.1.0
   */
  public static Tag create(TagKey key, TagValue value, TagMetadata tagMetadata) {
    return new AutoValue_Tag(key, value, tagMetadata);
  }

  /**
   * Returns the tag's key.
   *
   * @return the tag's key.
   * @since 0.1.0
   */
  public abstract TagKey getKey();

  /**
   * Returns the tag's value.
   *
   * @return the tag's value.
   * @since 0.1.0
   */
  public abstract TagValue getValue();

  /**
   * Returns the {@link TagMetadata} associated with this {@link Tag}.
   *
   * @return the {@code TagMetadata}.
   * @since 0.1.0
   */
  public abstract TagMetadata getTagMetadata();
}
