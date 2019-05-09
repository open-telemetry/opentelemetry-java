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

/** Tests for {@link TagMetadata}. */
@RunWith(JUnit4.class)
public class TagMetadataTest {

  @Test
  public void testGetTagTtl() {
    TagMetadata tagMetadata = TagMetadata.create(TagTtl.NO_PROPAGATION);
    assertThat(tagMetadata.getTagTtl()).isEqualTo(TagTtl.NO_PROPAGATION);
  }

  @Test
  public void testEquals() {
    new EqualsTester()
        .addEqualityGroup(
            TagMetadata.create(TagTtl.NO_PROPAGATION), TagMetadata.create(TagTtl.NO_PROPAGATION))
        .addEqualityGroup(TagMetadata.create(TagTtl.UNLIMITED_PROPAGATION))
        .testEquals();
  }
}
