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

import com.google.common.testing.EqualsTester;
import io.opentelemetry.tags.TagMetadata.TagTtl;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Tests for {@link Tag}. */
@RunWith(JUnit4.class)
public final class TagTest {

  private static final TagKey KEY = TagKey.create("KEY");
  private static final TagKey KEY_2 = TagKey.create("KEY2");
  private static final TagValue VALUE = TagValue.create("VALUE");
  private static final TagValue VALUE_2 = TagValue.create("VALUE2");
  private static final TagMetadata METADATA_UNLIMITED_PROPAGATION =
      TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION);
  private static final TagMetadata METADATA_NO_PROPAGATION =
      TagMetadata.create(TagTtl.NO_PROPAGATION);

  @Test
  public void testGetKey() {
    assertThat(Tag.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION).getKey()).isEqualTo(KEY);
  }

  @Test
  public void testGetTagMetadata() {
    assertThat(Tag.create(KEY, VALUE, METADATA_NO_PROPAGATION).getTagMetadata())
        .isEqualTo(METADATA_NO_PROPAGATION);
  }

  @Test
  public void testTagEquals() {
    new EqualsTester()
        .addEqualityGroup(
            Tag.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION),
            Tag.create(KEY, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Tag.create(KEY, VALUE_2, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Tag.create(KEY_2, VALUE, METADATA_UNLIMITED_PROPAGATION))
        .addEqualityGroup(Tag.create(KEY, VALUE, METADATA_NO_PROPAGATION))
        .testEquals();
  }
}
