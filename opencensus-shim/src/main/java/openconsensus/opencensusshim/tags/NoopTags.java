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

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.annotation.concurrent.Immutable;
import javax.annotation.concurrent.ThreadSafe;
import openconsensus.opencensusshim.common.Scope;
import openconsensus.opencensusshim.internal.NoopScope;
import openconsensus.opencensusshim.internal.Utils;
import openconsensus.opencensusshim.tags.propagation.TagContextBinarySerializer;
import openconsensus.opencensusshim.tags.propagation.TagContextDeserializationException;
import openconsensus.opencensusshim.tags.propagation.TagContextSerializationException;
import openconsensus.opencensusshim.tags.propagation.TagContextTextFormat;
import openconsensus.opencensusshim.tags.propagation.TagPropagationComponent;

/*>>>
import org.checkerframework.checker.nullness.qual.NonNull;
*/

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

  /**
   * Returns a {@code TagContextTextFormat} that serializes all {@code TagContext}s to empty strings
   * and deserializes all inputs to empty {@code TagContext}s.
   */
  static TagContextTextFormat getNoopTagContextTextSerializer() {
    return NoopTagContextTextFormat.INSTANCE;
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

    @Override
    public TagContextTextFormat getCorrelationContextFormat() {
      return getNoopTagContextTextSerializer();
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

  @Immutable
  private static final class NoopTagContextTextFormat extends TagContextTextFormat {

    static final NoopTagContextTextFormat INSTANCE = new NoopTagContextTextFormat();

    @Override
    public List<String> fields() {
      return Collections.<String>emptyList();
    }

    @Override
    public <C /*>>> extends @NonNull Object*/> void inject(
        TagContext tagContext, C carrier, Setter<C> setter)
        throws TagContextSerializationException {
      Utils.checkNotNull(tagContext, "tagContext");
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(setter, "setter");
    }

    @Override
    public <C /*>>> extends @NonNull Object*/> TagContext extract(C carrier, Getter<C> getter)
        throws TagContextDeserializationException {
      Utils.checkNotNull(carrier, "carrier");
      Utils.checkNotNull(getter, "getter");
      return getNoopTagContext();
    }
  }
}
