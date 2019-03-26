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
import java.util.Iterator;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.common.Scope;
import openconsensus.internal.NoopScope;
import openconsensus.internal.Utils;
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
   * Returns a {@code Tagger} that only produces {@link TagContext}s with no tags.
   *
   * @return a {@code Tagger} that only produces {@code TagContext}s with no tags.
   */
  static Tagger getNoopTagger() {
    return NoopTagger.INSTANCE;
  }

  /**
   * Returns a {@code TagContextBuilder} that ignores all calls to {@link TagContextBuilder#put}.
   *
   * @return a {@code TagContextBuilder} that ignores all calls to {@link TagContextBuilder#put}.
   */
  static TagContextBuilder getNoopTagContextBuilder() {
    return NoopTagContextBuilder.INSTANCE;
  }

  /**
   * Returns a {@code TagContext} that does not contain any tags.
   *
   * @return a {@code TagContext} that does not contain any tags.
   */
  static TagContext getNoopTagContext() {
    return NoopTagContext.INSTANCE;
  }

  /** Returns a {@code TagPropagationComponent} that contains no-op serializers. */
  static TagPropagationComponent getNoopTagPropagationComponent() {
    return NoopTagPropagationComponent.INSTANCE;
  }

  /**
   * Returns a {@code TagContextBinarySerializer} that serializes all {@code TagContext}s to zero
   * bytes and deserializes all inputs to empty {@code TagContext}s.
   */
  static TagContextBinarySerializer getNoopTagContextBinarySerializer() {
    return NoopTagContextBinarySerializer.INSTANCE;
  }

  @ThreadSafe
  private static final class NoopTagsComponent extends TagsComponent {
    private volatile boolean isRead;

    @Override
    public Tagger getTagger() {
      return getNoopTagger();
    }

    @Override
    public TagPropagationComponent getTagPropagationComponent() {
      return getNoopTagPropagationComponent();
    }

    @Override
    public TaggingState getState() {
      isRead = true;
      return TaggingState.DISABLED;
    }

    @Override
    @Deprecated
    public void setState(TaggingState state) {
      Utils.checkNotNull(state, "state");
      Utils.checkState(!isRead, "State was already read, cannot set state.");
    }
  }

  @Immutable
  private static final class NoopTagger extends Tagger {
    static final Tagger INSTANCE = new NoopTagger();

    @Override
    public TagContext empty() {
      return getNoopTagContext();
    }

    @Override
    public TagContext getCurrentTagContext() {
      return getNoopTagContext();
    }

    @Override
    public TagContextBuilder emptyBuilder() {
      return getNoopTagContextBuilder();
    }

    @Override
    public TagContextBuilder toBuilder(TagContext tags) {
      Utils.checkNotNull(tags, "tags");
      return getNoopTagContextBuilder();
    }

    @Override
    public TagContextBuilder currentBuilder() {
      return getNoopTagContextBuilder();
    }

    @Override
    public Scope withTagContext(TagContext tags) {
      Utils.checkNotNull(tags, "tags");
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagContextBuilder extends TagContextBuilder {
    static final TagContextBuilder INSTANCE = new NoopTagContextBuilder();

    @Override
    @SuppressWarnings("deprecation")
    public TagContextBuilder put(TagKey key, TagValue value) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      return this;
    }

    @Override
    public TagContextBuilder put(TagKey key, TagValue value, TagMetadata tagMetadata) {
      Utils.checkNotNull(key, "key");
      Utils.checkNotNull(value, "value");
      Utils.checkNotNull(tagMetadata, "tagMetadata");
      return this;
    }

    @Override
    public TagContextBuilder remove(TagKey key) {
      Utils.checkNotNull(key, "key");
      return this;
    }

    @Override
    public TagContext build() {
      return getNoopTagContext();
    }

    @Override
    public Scope buildScoped() {
      return NoopScope.getInstance();
    }
  }

  @Immutable
  private static final class NoopTagContext extends TagContext {
    static final TagContext INSTANCE = new NoopTagContext();

    // TODO(sebright): Is there any way to let the user know that their tags were ignored?
    @Override
    protected Iterator<Tag> getIterator() {
      return Collections.<Tag>emptySet().iterator();
    }
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
    public byte[] toByteArray(TagContext tags) {
      Utils.checkNotNull(tags, "tags");
      return EMPTY_BYTE_ARRAY;
    }

    @Override
    public TagContext fromByteArray(byte[] bytes) {
      Utils.checkNotNull(bytes, "bytes");
      return getNoopTagContext();
    }
  }
}
