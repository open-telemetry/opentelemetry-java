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

package openconsensus.tags.unsafe;

import static com.google.common.truth.Truth.assertThat;

import com.google.common.collect.Lists;
import io.grpc.Context;
import java.util.List;
import openconsensus.tags.Tag;
import openconsensus.tags.TagMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextUtils}. */
@RunWith(JUnit4.class)
public final class ContextUtilsTest {
  @Test
  public void testContextKeyName() {
    // Context.Key.toString() returns the name.
    assertThat(ContextUtils.TAG_MAP_KEY.toString()).isEqualTo("openconsensus-tag-map-key");
  }

  @Test
  public void testGetCurrentTagMap_DefaultContext() {
    TagMap tags = ContextUtils.TAG_MAP_KEY.get();
    assertThat(tags).isNotNull();
    assertThat(asList(tags)).isEmpty();
  }

  @Test
  public void testGetCurrentTagMap_ContextSetToNull() {
    Context orig = Context.current().withValue(ContextUtils.TAG_MAP_KEY, null).attach();
    try {
      TagMap tags = ContextUtils.TAG_MAP_KEY.get();
      assertThat(tags).isNotNull();
      assertThat(asList(tags)).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  private static List<Tag> asList(TagMap tags) {
    return Lists.newArrayList(tags.getIterator());
  }
}
