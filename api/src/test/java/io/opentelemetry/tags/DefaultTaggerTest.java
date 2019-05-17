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
import io.opentelemetry.context.NoopScope;
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
  private static final Tagger noopTagger = DefaultTagger.getInstance();
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
  public void defaultMethods() {
    assertThat(asList(noopTagger.getCurrentTagMap())).isEmpty();
    assertThat(asList(noopTagger.emptyBuilder().build())).isEmpty();
    assertThat(asList(noopTagger.toBuilder(TAG_MAP).build())).isEmpty();
    assertThat(asList(noopTagger.currentBuilder().build())).isEmpty();
    assertThat(noopTagger.withTagMap(TAG_MAP)).isSameAs(NoopScope.INSTANCE);
  }

  @Test
  public void toBuilder_DisallowsNull() {
    thrown.expect(NullPointerException.class);
    noopTagger.toBuilder(null);
  }

  @Test
  public void withTagMap_DisallowsNull() {
    thrown.expect(NullPointerException.class);
    noopTagger.withTagMap(null);
  }

  @Test
  public void noopTagMapBuilder_Put_DisallowsNullKey() {
    TagMap.Builder noopBuilder = noopTagger.currentBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(null, VALUE, Tag.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopTagMapBuilder_Put_DisallowsNullValue() {
    TagMap.Builder noopBuilder = noopTagger.currentBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, null, Tag.METADATA_UNLIMITED_PROPAGATION);
  }

  @Test
  public void noopTagMapBuilder_Put_DisallowsNullTagMetadata() {
    TagMap.Builder noopBuilder = noopTagger.currentBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.put(KEY, VALUE, null);
  }

  @Test
  public void noopTagMapBuilder_Remove_DisallowsNullKey() {
    TagMap.Builder noopBuilder = noopTagger.currentBuilder();
    thrown.expect(NullPointerException.class);
    noopBuilder.remove(null);
  }

  private static List<Tag> asList(TagMap tags) {
    return Lists.newArrayList(tags.getIterator());
  }
}
