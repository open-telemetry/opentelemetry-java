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

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.tags.unsafe.ContextUtils;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link DefaultTagger}. */
@RunWith(JUnit4.class)
public final class DefaultTaggerTest {
  private static final Tagger defaultTagger = DefaultTagger.getInstance();
  private static final TagKey KEY = TagKey.create("key");
  private static final TagValue VALUE = TagValue.create("value");

  private static final TagMap TAG_MAP =
      new TagMap() {

        @Override
        public Iterator<Tag> getIterator() {
          return Arrays.asList(Tag.create(KEY, VALUE, Tag.METADATA_UNLIMITED_PROPAGATION))
              .iterator();
        }

        @Nullable
        @Override
        public TagValue getTagValue(TagKey tagKey) {
          return VALUE;
        }
      };

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void builderMethod() {
    assertThat(asList(defaultTagger.tagMapBuilder().build())).isEmpty();
  }

  @Test
  public void getCurrentTagMap_DefaultContext() {
    assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
  }

  @Test
  public void getCurrentTagMap_ContextSetToNull() {
    Context orig = ContextUtils.withValue(null).attach();
    try {
      TagMap tags = defaultTagger.getCurrentTagMap();
      assertThat(tags).isNotNull();
      assertThat(tags.getIterator().hasNext()).isFalse();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void withTagMap() {
    assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
    Scope wtm = defaultTagger.withTagMap(TAG_MAP);
    try {
      assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(TAG_MAP);
    } finally {
      wtm.close();
    }
    assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
  }

  @Test
  public void withTagMap_nullTagMap() {
    assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
    Scope wtm = defaultTagger.withTagMap(null);
    try {
      assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
    } finally {
      wtm.close();
    }
    assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
  }

  @Test
  public void withTagMapUsingWrap() {
    Runnable runnable;
    Scope wtm = defaultTagger.withTagMap(TAG_MAP);
    try {
      assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(TAG_MAP);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(TAG_MAP);
                    }
                  });
    } finally {
      wtm.close();
    }
    assertThat(defaultTagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.INSTANCE);
    // When we run the runnable we will have the TagMap in the current Context.
    runnable.run();
  }

  @Test
  public void noopTagMapBuilder_SetParent_DisallowsNullKey() {
    TagMap.Builder noopBuilder = defaultTagger.tagMapBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.setParent(null);
  }

  @Test
  public void noopTagMapBuilder_Put_DisallowsNullKey() {
    TagMap.Builder noopBuilder = defaultTagger.tagMapBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE, Tag.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopTagMapBuilder_Put_DisallowsNullValue() {
    TagMap.Builder noopBuilder = defaultTagger.tagMapBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, null, Tag.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopTagMapBuilder_Put_DisallowsNullTagMetadata() {
    TagMap.Builder noopBuilder = defaultTagger.tagMapBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, VALUE, null);
  }

  @Test
  public void noopTagMapBuilder_Remove_DisallowsNullKey() {
    TagMap.Builder noopBuilder = defaultTagger.tagMapBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }

  private static List<Tag> asList(TagMap tags) {
    return Lists.newArrayList(tags.getIterator());
  }
}
