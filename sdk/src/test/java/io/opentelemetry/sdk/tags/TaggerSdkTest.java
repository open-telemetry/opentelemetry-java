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

import io.grpc.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.tags.EmptyTagMap;
import io.opentelemetry.tags.TagMap;
import io.opentelemetry.tags.unsafe.ContextUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/** Unit tests for {@link TaggerSdk}. */
@RunWith(JUnit4.class)
public class TaggerSdkTest {
  @Mock private TagMap tagMap;
  private final TaggerSdk tagger = new TaggerSdk();

  @Before
  public void setUp() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testGetCurrentTagMap_DefaultContext() {
    assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.getInstance());
  }

  @Test
  public void testGetCurrentTagMap_ContextSetToNull() {
    Context orig = ContextUtils.withValue(null).attach();
    try {
      TagMap tags = tagger.getCurrentTagMap();
      assertThat(tags).isNotNull();
      assertThat(tags.getIterator().hasNext()).isFalse();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testWithTagMap() {
    assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.getInstance());
    try (Scope wtm = tagger.withTagMap(tagMap)) {
      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(tagMap);
    }
    assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.getInstance());
  }

  @Test
  public void testWithTagMapUsingWrap() {
    Runnable runnable;
    try (Scope wtm = tagger.withTagMap(tagMap)) {
      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(tagMap);
      runnable =
          Context.current()
              .wrap(
                  new Runnable() {
                    @Override
                    public void run() {
                      assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(tagMap);
                    }
                  });
    }
    assertThat(tagger.getCurrentTagMap()).isSameInstanceAs(EmptyTagMap.getInstance());
    // When we run the runnable we will have the TagMap in the current Context.
    runnable.run();
  }
}
