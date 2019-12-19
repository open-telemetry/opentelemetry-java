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

package io.opentelemetry.correlationcontext.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.correlationcontext.CorrelationContext;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link ContextUtils}. */
@RunWith(JUnit4.class)
public final class ContextUtilsTest {
  @Test
  public void testGetCurrentDistributedContext_DefaultContext() {
    CorrelationContext distContext = ContextUtils.getCorrelationContext(Context.current());
    assertThat(distContext).isNull();
  }

  @Test
  public void testGetCurrentDistributedContex_DefaultContext_WithoutExplicitContext() {
    CorrelationContext distContext = ContextUtils.getCorrelationContext();
    assertThat(distContext).isNull();
  }

  @Test
  public void testGetCurrentDistributedContextWithDefault_DefaultContext() {
    CorrelationContext distContext =
        ContextUtils.getCorrelationContextWithDefault(Context.current());
    assertThat(distContext).isNotNull();
    assertThat(distContext.getEntries()).isEmpty();
  }

  @Test
  public void testGetCurrentDistributedContextWithDefault_ContextSetToNull() {
    Context orig = ContextUtils.withCorrelationContext(null, Context.current()).attach();
    try {
      CorrelationContext distContext =
          ContextUtils.getCorrelationContextWithDefault(Context.current());
      assertThat(distContext).isNotNull();
      assertThat(distContext.getEntries()).isEmpty();
    } finally {
      Context.current().detach(orig);
    }
  }
}
