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

package io.opentelemetry.sdk.tags;

import static com.google.common.truth.Truth.assertThat;
import static io.opentelemetry.sdk.tags.TagMapTestUtil.tagMapToList;

import io.opentelemetry.context.Scope;
import io.opentelemetry.tags.EmptyTagMap;
import io.opentelemetry.tags.Tag;
import io.opentelemetry.tags.TagKey;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.TagMetadata;
import io.opentelemetry.tags.TagValue;
import io.opentelemetry.tags.Tagger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Unit tests for the methods in {@link TaggerSdk} and {@link TagMapSdk.Builder} that interact with
 * the current {@link TagMapSdk}.
 */
@RunWith(JUnit4.class)
public class ScopedTagMapTest {
  private static final TagKey KEY_1 = TagKey.create("key 1");
  private static final TagKey KEY_2 = TagKey.create("key 2");
  private static final TagKey KEY_3 = TagKey.create("key 3");

  private static final TagValue VALUE_1 = TagValue.create("value 1");
  private static final TagValue VALUE_2 = TagValue.create("value 2");
  private static final TagValue VALUE_3 = TagValue.create("value 3");
  private static final TagValue VALUE_4 = TagValue.create("value 4");

  private static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION);
  private static final TagMetadata METADATA_NO_PROPAGATION =
      TagMetadata.create(TagMetadata.TagTtl.NO_PROPAGATION);

  private final Tagger tagger = new TaggerSdk();

  @Test
  public void emptyTagMap() {
    TagMap defaultTagMap = tagger.getCurrentTagMap();
    assertThat(tagMapToList(defaultTagMap)).isEmpty();
    assertThat(defaultTagMap).isInstanceOf(EmptyTagMap.class);
  }

  @Test
  public void withTagMap() {
    assertThat(tagMapToList(tagger.getCurrentTagMap())).isEmpty();
    TagMap scopedTags =
        tagger.tagMapBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = tagger.withTagMap(scopedTags)) {
      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(scopedTags);
    }
    assertThat(tagMapToList(tagger.getCurrentTagMap())).isEmpty();
  }

  @Test
  public void createBuilderFromCurrentTags() {
    TagMap scopedTags =
        tagger.tagMapBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope = tagger.withTagMap(scopedTags)) {
      TagMap newTags =
          tagger
              .tagMapBuilder()
              .setParent(tagger.getCurrentTagMap())
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .build();
      assertThat(tagMapToList(newTags))
          .containsExactly(
              Tag.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
              Tag.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(scopedTags);
    }
  }

  @Test
  public void setCurrentTagsWithBuilder() {
    assertThat(tagMapToList(tagger.getCurrentTagMap())).isEmpty();
    try (Scope scope =
        tagger.tagMapBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).buildScoped()) {
      assertThat(tagMapToList(tagger.getCurrentTagMap()))
          .containsExactly(Tag.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION));
    }
    assertThat(tagMapToList(tagger.getCurrentTagMap())).isEmpty();
  }

  @Test
  public void addToCurrentTagsWithBuilder() {
    TagMap scopedTags =
        tagger.tagMapBuilder().put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION).build();
    try (Scope scope1 = tagger.withTagMap(scopedTags)) {
      try (Scope scope2 =
          tagger
              .tagMapBuilder()
              .setParent(tagger.getCurrentTagMap())
              .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
              .buildScoped()) {
        assertThat(tagMapToList(tagger.getCurrentTagMap()))
            .containsExactly(
                Tag.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Tag.create(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION));
      }
      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(scopedTags);
    }
  }

  @Test
  public void multiScopeTagMapWithMetadata() {
    TagMap scopedTags =
        tagger
            .tagMapBuilder()
            .put(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION)
            .put(KEY_2, VALUE_2, METADATA_UNLIMITED_PROPAGATION)
            .build();
    try (Scope scope1 = tagger.withTagMap(scopedTags)) { // Scope 1
      try (Scope scope2 =
          tagger
              .tagMapBuilder()
              .setParent(tagger.getCurrentTagMap())
              .put(KEY_3, VALUE_3, METADATA_NO_PROPAGATION)
              .put(KEY_2, VALUE_4, METADATA_NO_PROPAGATION)
              .buildScoped()) { // Scope 2
        assertThat(tagMapToList(tagger.getCurrentTagMap()))
            .containsExactly(
                Tag.create(KEY_1, VALUE_1, METADATA_UNLIMITED_PROPAGATION),
                Tag.create(KEY_2, VALUE_4, METADATA_NO_PROPAGATION),
                Tag.create(KEY_3, VALUE_3, METADATA_NO_PROPAGATION));
      }
      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(scopedTags);
    }
  }
}
