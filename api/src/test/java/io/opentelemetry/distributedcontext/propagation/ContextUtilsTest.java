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

package io.opentelemetry.distributedcontext.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.distributedcontext.DistributedContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextUtils}. */
@RunWith(JUnit4.class)
public final class ContextUtilsTest {
  @Test
  public void testGetCurrentDistributedContex_DefaultContext() {
    DistributedContext distContext = ContextUtils.getValue(Context.current());
    assertThat(distContext).isNotNull();
    assertThat(distContext.getEntries()).isEmpty();
  }

  @Test
  public void testGetCurrentDistributedContex_DefaultContext_WithoutExplicitContext() {
    DistributedContext distContext = ContextUtils.getValue();
    assertThat(distContext).isNotNull();
    assertThat(distContext.getEntries()).isEmpty();
  }

  @Test
  public void testGetCurrentDistributedContex_ContextSetToNull() {
    Context orig = ContextUtils.withValue(null, Context.current()).attach();
    try {
      DistributedContext distContext = ContextUtils.getValue(Context.current());
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testGetCurrentDistributedContex_ContextSetToNull_WithoutExplicitContext() {
    Context orig = ContextUtils.withValue(null).attach();
    try {
      DistributedContext distContext = ContextUtils.getValue();
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }
}
