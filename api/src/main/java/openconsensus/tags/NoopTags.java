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

import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import openconsensus.context.NoopScope;
import openconsensus.context.Scope;
import openconsensus.context.propagation.BinaryFormat;
import openconsensus.context.propagation.TextFormat;
import openconsensus.internal.Utils;

/**
 * No-op implementations of tagging classes.
 *
 * @since 0.1.0
 */
public final class NoopTags {

  private NoopTags() {}

  /**
   * Returns a {@code Tagger} that is a no-op implementation for {@link Tagger}.
   *
   * @return a {@code Tagger} that is a no-op implementation for {@link Tagger}.
   * @since 0.1.0
   */
  public static Tagger newNoopTagger() {
    return new NoopTagger();
  }

  @Immutable
  private static final class NoopTagger extends Tagger {
    private static final BinaryFormat<TagMap> BINARY_FORMAT = new NoopBinaryFormat();
    private static final TextFormat<TagMap> TEXT_FORMAT = new NoopTextFormat();

    @Override
    public TagMap empty() {
      return EmptyTagMap.INSTANCE;
    }

    @Override
    public TagMap getCurrentTagMap() {
      return EmptyTagMap.INSTANCE;
    }

    @Override
    public TagMapBuilder emptyBuilder() {
      return new NoopTagMapBuilder();
    }

    @Override
    public TagMapBuilder toBuilder(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return new NoopTagMapBuilder();
    }

    @Override
    public TagMapBuilder currentBuilder() {
      return new NoopTagMapBuilder();
    }

    @Override
    public Scope withTagMap(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return NoopScope.getInstance();
    }

    @Override
    public BinaryFormat<TagMap> getBinaryFormat() {
      return BINARY_FORMAT;
    }

    @Override
    public TextFormat<TagMap> getTextFormat() {
      return TEXT_FORMAT;
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
      return EmptyTagMap.INSTANCE;
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopBinaryFormat
      extends openconsensus.context.propagation.BinaryFormat<TagMap> {
    static final byte[] EMPTY_BYTE_ARRAY = {};

    @Override
    public byte[] toByteArray(TagMap tags) {
      Utils.checkNotNull(tags, "tags");
      return EMPTY_BYTE_ARRAY;
    }

    @Override
    public TagMap fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return EmptyTagMap.INSTANCE;
    }
  }

  @Immutable
  private static final class NoopTextFormat
      extends openconsensus.context.propagation.TextFormat<TagMap> {
    @Override
    public List<String> fields() {
      return Collections.emptyList();
    }

    @Override
    public <C> void inject(TagMap tagContext, C carrier, Setter<C> setter) {
      Utils.checkNotNull(tagContext, "tagContext");
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(setter, "setter");
    }

    @Override
    public <C> TagMap extract(C carrier, Getter<C> getter) {
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(getter, "getter");
      return EmptyTagMap.INSTANCE;
    }
  }
}
