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

package io.opentelemetry.tags.unsafe;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.tags.TagMap;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextUtils}. */
@RunWith(JUnit4.class)
public final class ContextUtilsTest {
  @Test
  public void testGetCurrentTagMap_DefaultContext() {
    TagMap tags = ContextUtils.getValue(Context.current());
    assertThat(tags).isNotNull();
    assertThat(tags.getIterator().hasNext()).isFalse();
  }

  @Test
  public void testGetCurrentTagMap_ContextSetToNull() {
    Context orig = ContextUtils.withValue(Context.current(), null).attach();
    try {
      TagMap tags = ContextUtils.getValue(Context.current());
      assertThat(tags).isNotNull();
      assertThat(tags.getIterator().hasNext()).isFalse();
    } finally {
      Context.current().detach(orig);
    }
  }
}
