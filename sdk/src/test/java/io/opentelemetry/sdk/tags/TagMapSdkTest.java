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
import static io.opentelemetry.sdk.tags.TagMapTestUtil.listToTagMap;
import static io.opentelemetry.sdk.tags.TagMapTestUtil.tagMapToList;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import com.google.common.testing.EqualsTester;
import io.opentelemetry.tags.Tag;
import io.opentelemetry.tags.TagKey;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.TagMetadata;
import io.opentelemetry.tags.TagValue;
import io.opentelemetry.tags.Tagger;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/**
 * Tests for {@link TagMapSdk} and {@link TagMapSdk.Builder}.
 *
 * <p>Tests for {@link TagMapSdk.Builder#buildScoped()} are in {@link ScopedTagMapTest}.
 */
@RunWith(JUnit4.class)
public class TagMapSdkTest {
  private final Tagger tagger = new TaggerSdk();

  private static final TagMetadata TMD =
      TagMetadata.create(TagMetadata.TagTtl.UNLIMITED_PROPAGATION);

  private static final TagKey K1 = TagKey.create("k1");
  private static final TagKey K2 = TagKey.create("k2");

  private static final TagValue V1 = TagValue.create("v1");
  private static final TagValue V2 = TagValue.create("v2");

  private static final Tag T1 = Tag.create(K1, V1, TMD);
  private static final Tag T2 = Tag.create(K2, V2, TMD);

  @Rule public final ExpectedException thrown = ExpectedException.none();

  @Test
  public void getIterator_empty() {
    TagMapSdk tags = new TagMapSdk.Builder().build();
    assertThat(tagMapToList(tags)).isEmpty();
  }

  @Test
  public void getIterator_nonEmpty() {
    TagMapSdk tags = listToTagMap(T1, T2);
    assertThat(tagMapToList(tags)).containsExactly(T1, T2);
  }

  @Test
  public void getIterator_chain() {
    Tag t1alt = Tag.create(K1, V2, TMD);
    TagMapSdk parent = listToTagMap(T1, T2);
    TagMap tags =
        tagger
            .tagMapBuilder()
            .setParent(parent)
            .put(t1alt.getKey(), t1alt.getValue(), t1alt.getTagMetadata())
            .build();
    assertThat(tagMapToList(tags)).containsExactly(t1alt, T2);
  }

  @Test
  public void put_newKey() {
    TagMapSdk tags = listToTagMap(T1);
    assertThat(tagMapToList(tagger.tagMapBuilder().setParent(tags).put(K2, V2, TMD).build()))
        .containsExactly(T1, T2);
  }

  @Test
  public void put_existingKey() {
    TagMapSdk tags = listToTagMap(T1);
    assertThat(tagMapToList(tagger.tagMapBuilder().setParent(tags).put(K1, V2, TMD).build()))
        .containsExactly(Tag.create(K1, V2, TMD));
  }

  @Test
  public void put_nullKey() {
    TagMapSdk tags = listToTagMap(T1);
    TagMap.Builder builder = tagger.tagMapBuilder().setParent(tags);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    builder.put(null, V2, TMD);
  }

  @Test
  public void put_nullValue() {
    TagMapSdk tags = listToTagMap(T1);
    TagMap.Builder builder = tagger.tagMapBuilder().setParent(tags);
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("value");
    builder.put(K2, null, TMD);
  }

  @Test
  public void setParent_nullValue() {
    TagMapSdk parent = listToTagMap(T1);
    TagMap tags = tagger.tagMapBuilder().setParent(parent).setParent(null).build();
    assertThat(tagMapToList(tags)).isEmpty();
  }

  @Test
  public void remove_existingKey() {
    TagMapSdk.Builder builder = new TagMapSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getTagMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getTagMetadata());

    assertThat(tagMapToList(builder.remove(K1).build())).containsExactly(T2);
  }

  @Test
  public void remove_differentKey() {
    TagMapSdk.Builder builder = new TagMapSdk.Builder();
    builder.put(T1.getKey(), T1.getValue(), T1.getTagMetadata());
    builder.put(T2.getKey(), T2.getValue(), T2.getTagMetadata());

    assertThat(tagMapToList(builder.remove(K2).build())).containsExactly(T1);
  }

  @Test
  public void remove_keyFromParent() {
    TagMapSdk tags = listToTagMap(T1, T2);
    assertThat(tagMapToList(tagger.tagMapBuilder().setParent(tags).remove(K1).build()))
        .containsExactly(T2);
  }

  @Test
  public void remove_nullKey() {
    TagMap.Builder builder = tagger.tagMapBuilder();
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("key");
    builder.remove(null);
  }

  @Test
  public void testIterator() {
    TagMap tags =
        tagger
            .tagMapBuilder()
            .setParent(listToTagMap(T1))
            .put(T2.getKey(), T2.getValue(), T2.getTagMetadata())
            .build();
    Iterator<Tag> i = tags.getIterator();
    assertTrue(i.hasNext());
    Tag tag1 = i.next();
    assertTrue(i.hasNext());
    Tag tag2 = i.next();
    assertFalse(i.hasNext());
    assertThat(Arrays.asList(tag1, tag2))
        .containsExactly(Tag.create(K1, V1, TMD), Tag.create(K2, V2, TMD));
    thrown.expect(NoSuchElementException.class);
    i.next();
  }

  @Test
  public void disallowCallingRemoveOnIterator() {
    TagMapSdk tags = listToTagMap(T1, T2);
    Iterator<Tag> i = tags.getIterator();
    i.next();
    thrown.expect(UnsupportedOperationException.class);
    i.remove();
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            tagger.tagMapBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            tagger.tagMapBuilder().put(K1, V1, TMD).put(K2, V2, TMD).build(),
            tagger.tagMapBuilder().put(K2, V2, TMD).put(K1, V1, TMD).build())
        .addEqualityGroup(tagger.tagMapBuilder().put(K1, V1, TMD).put(K2, V1, TMD).build())
        .addEqualityGroup(tagger.tagMapBuilder().put(K1, V2, TMD).put(K2, V1, TMD).build())
        .testEquals();
  }
}
