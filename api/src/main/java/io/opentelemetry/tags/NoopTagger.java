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

package io.opentelemetry.tags;

import io.opentelemetry.context.NoopScope;
import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.BinaryFormat;
import io.opentelemetry.context.propagation.HttpTextFormat;
import io.opentelemetry.internal.Utils;
import java.util.Collections;
import java.util.List;
import javax.annotation.concurrent.Immutable;

/**
 * No-op implementations of {@link Tagger}.
 *
 * @since 0.1.0
 */
@Immutable
public final class NoopTagger implements Tagger {
  private static final NoopTagger INSTANCE = new NoopTagger();
  private static final BinaryFormat<TagMap> BINARY_FORMAT = new NoopBinaryFormat();
  private static final HttpTextFormat<TagMap> HTTP_TEXT_FORMAT = new NoopHttpTextFormat();

  /**
   * Returns a {@code Tagger} singleton that is a no-op implementation for {@link Tagger}.
   *
   * @return a {@code Tagger} singleton that is a no-op implementation for {@link Tagger}.
   * @since 0.1.0
   */
  public static Tagger getInstance() {
    return INSTANCE;
  }

  @Override
  public TagMap getCurrentTagMap() {
    return EmptyTagMap.INSTANCE;
  }

  @Override
  public TagMap.Builder emptyBuilder() {
    return new NoopTagMapBuilder();
  }

  @Override
  public TagMap.Builder toBuilder(TagMap tags) {
    Utils.checkNotNull(tags, "tags");
    return new NoopTagMapBuilder();
  }

  @Override
  public TagMap.Builder currentBuilder() {
    return new NoopTagMapBuilder();
  }

  @Override
  public Scope withTagMap(TagMap tags) {
    Utils.checkNotNull(tags, "tags");
    return NoopScope.INSTANCE;
  }

  @Override
  public BinaryFormat<TagMap> getBinaryFormat() {
    return BINARY_FORMAT;
  }

  @Override
  public HttpTextFormat<TagMap> getHttpTextFormat() {
    return HTTP_TEXT_FORMAT;
  }

  @Immutable
  private static final class NoopTagMapBuilder implements TagMap.Builder {
    @Override
    public TagMap.Builder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(tagMetadata, "tagMetadata");
      return this;
    }

    @Override
    public TagMap.Builder remove(TagKey key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public TagMap build() {
      return EmptyTagMap.INSTANCE;
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.INSTANCE;
    }
  }

  @Immutable
  private static final class NoopBinaryFormat implements BinaryFormat<TagMap> {
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
  private static final class NoopHttpTextFormat implements HttpTextFormat<TagMap> {
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
