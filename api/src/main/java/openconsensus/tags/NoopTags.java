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

import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.context.NoopScope;
import openconsensus.context.Scope;
import openconsensus.internal.Utils;
import openconsensus.tags.data.TagKey;
import openconsensus.tags.data.TagMetadata;
import openconsensus.tags.data.TagValue;
import openconsensus.tags.propagation.TagMapBinarySerializer;
import openconsensus.tags.propagation.TagPropagationComponent;

/** No-op implementations of tagging classes. */
final class NoopTags {

  private NoopTags() {}

  /**
   * Returns a {@code TagsComponent} that has a no-op implementation for {@link Tagger}.
   *
   * @return a {@code TagsComponent} that has a no-op implementation for {@code Tagger}.
   */
  static TagsComponent newNoopTagsComponent() {
    return new NoopTagsComponent();
  }

  /**
   * Returns a {@code Tagger} that only produces {@link TagMap}s with no tags.
   *
   * @return a {@code Tagger} that only produces {@code TagMap}s with no tags.
   */
  static Tagger getNoopTagger() {
    return new NoopTagger();
  }

  /**
   * Returns a {@code TagMapBuilder} that ignores all calls to {@link TagMapBuilder#put}.
   *
   * @return a {@code TagMapBuilder} that ignores all calls to {@link TagMapBuilder#put}.
   */
  static TagMapBuilder getNoopTagMapBuilder() {
    return new NoopTagMapBuilder();
  }

  /**
   * Returns a {@code TagMap} that does not contain any tags.
   *
   * @return a {@code TagMap} that does not contain any tags.
   */
  static TagMap getNoopTagMap() {
    return new NoopTagMap();
  }

  /** Returns a {@code TagPropagationComponent} that contains no-op serializers. */
  static TagPropagationComponent getNoopTagPropagationComponent() {
    return new NoopTagPropagationComponent();
  }

  /**
   * Returns a {@code TagMapBinarySerializer} that serializes all {@code TagMap}s to zero bytes and
   * deserializes all inputs to empty {@code TagMap}s.
   */
  static TagMapBinarySerializer getNoopTagMapBinarySerializer() {
    return new NoopTagMapBinarySerializer();
  }

  @ThreadSafe
  private static final class NoopTagsComponent extends TagsComponent {
    @Override
    public Tagger getTagger() {
      return getNoopTagger();
    }

    @Override
    public TagPropagationComponent getTagPropagationComponent() {
      return getNoopTagPropagationComponent();
    }
  }

  @Immutable
  private static final class NoopTagger extends Tagger {

    @Override
    public TagMap empty() {
      return getNoopTagMap();
    }

    @Override
    public TagMap getCurrentTagMap() {
      return getNoopTagMap();
    }

    @Override
    public TagMapBuilder emptyBuilder() {
      return getNoopTagMapBuilder();
    }

    @Override
    public TagMapBuilder toBuilder(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return getNoopTagMapBuilder();
    }

    @Override
    public TagMapBuilder currentBuilder() {
      return getNoopTagMapBuilder();
    }

    @Override
    public Scope withTagMap(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagMapBuilder extends TagMapBuilder {

    @Override
    public TagMapBuilder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(tagMetadata, "tagMetadata");
      return this;
    }

    @Override
    public TagMapBuilder remove(TagKey key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public TagMap build() {
      return getNoopTagMap();
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagMap extends TagMap {}

  @Immutable
  private static final class NoopTagPropagationComponent extends TagPropagationComponent {

    @Override
    public TagMapBinarySerializer getBinarySerializer() {
      return getNoopTagMapBinarySerializer();
    }
  }

  @Immutable
  private static final class NoopTagMapBinarySerializer extends TagMapBinarySerializer {
    static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] toByteArray(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return EMPTY_BYTE_ARRAY;
    }

    @Override
    public TagMap fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return getNoopTagMap();
    }
  }
}
