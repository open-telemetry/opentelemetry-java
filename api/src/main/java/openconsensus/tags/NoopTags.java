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
import openconsensus.context.Scope;
import openconsensus.context.NoopScope;
import openconsensus.internal.Utils;
import openconsensus.tags.data.TagKey;
import openconsensus.tags.data.TagMetadata;
import openconsensus.tags.data.TagValue;
import openconsensus.tags.propagation.TagContextBinarySerializer;
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
    return NoopTagger.INSTANCE;
  }

  /**
   * Returns a {@code TagMapBuilder} that ignores all calls to {@link TagMapBuilder#put}.
   *
   * @return a {@code TagMapBuilder} that ignores all calls to {@link TagMapBuilder#put}.
   */
  static TagMapBuilder getNoopTagContextBuilder() {
    return NoopTagMapBuilder.INSTANCE;
  }

  /**
   * Returns a {@code TagMap} that does not contain any tags.
   *
   * @return a {@code TagMap} that does not contain any tags.
   */
  static TagMap getNoopTagContext() {
    return NoopTagMap.INSTANCE;
  }

  /** Returns a {@code TagPropagationComponent} that contains no-op serializers. */
  static TagPropagationComponent getNoopTagPropagationComponent() {
    return NoopTagPropagationComponent.INSTANCE;
  }

  /**
   * Returns a {@code TagContextBinarySerializer} that serializes all {@code TagMap}s to zero bytes
   * and deserializes all inputs to empty {@code TagMap}s.
   */
  static TagContextBinarySerializer getNoopTagContextBinarySerializer() {
    return NoopTagContextBinarySerializer.INSTANCE;
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
    static final Tagger INSTANCE = new NoopTagger();

    @Override
    public TagMap empty() {
      return getNoopTagContext();
    }

    @Override
    public TagMap getCurrentTagContext() {
      return getNoopTagContext();
    }

    @Override
    public TagMapBuilder emptyBuilder() {
      return getNoopTagContextBuilder();
    }

    @Override
    public TagMapBuilder toBuilder(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return getNoopTagContextBuilder();
    }

    @Override
    public TagMapBuilder currentBuilder() {
      return getNoopTagContextBuilder();
    }

    @Override
    public Scope withTagContext(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagMapBuilder extends TagMapBuilder {
    static final TagMapBuilder INSTANCE = new NoopTagMapBuilder();

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
      return getNoopTagContext();
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagMap extends TagMap {
    static final TagMap INSTANCE = new NoopTagMap();
  }

  @Immutable
  private static final class NoopTagPropagationComponent extends TagPropagationComponent {
    static final TagPropagationComponent INSTANCE = new NoopTagPropagationComponent();

    @Override
    public TagContextBinarySerializer getBinarySerializer() {
      return getNoopTagContextBinarySerializer();
    }
  }

  @Immutable
  private static final class NoopTagContextBinarySerializer extends TagContextBinarySerializer {
    static final TagContextBinarySerializer INSTANCE = new NoopTagContextBinarySerializer();
    static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] toByteArray(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return EMPTY_BYTE_ARRAY;
    }

    @Override
    public TagMap fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return getNoopTagContext();
    }
  }
}
