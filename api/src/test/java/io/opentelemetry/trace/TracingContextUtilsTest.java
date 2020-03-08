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

package io.opentelemetry.trace;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TracingContextUtils}. */
@RunWith(JUnit4.class)
public final class TracingContextUtilsTest {

  @Test
  public void testGetCurrentSpan_Default() {
    Span span = TracingContextUtils.getCurrentSpan();
    assertThat(span).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  @Test
  public void testGetCurrentSpan_SetSpan() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context orig = TracingContextUtils.withSpan(span, Context.current()).attach();
    try {
      assertThat(TracingContextUtils.getCurrentSpan()).isSameInstanceAs(span);
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testGetSpan_DefaultContext() {
    Span span = TracingContextUtils.getSpan(Context.current());
    assertThat(span).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  @Test
  public void testGetSpan_ExplicitContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context context = TracingContextUtils.withSpan(span, Context.current());
    assertThat(TracingContextUtils.getSpan(context)).isSameInstanceAs(span);
  }

  @Test
  public void testGetSpanWithoutDefault_DefaultContext() {
    Span span = TracingContextUtils.getSpanWithoutDefault(Context.current());
    assertThat(span).isNull();
  }

  @Test
  public void testGetSpanWithoutDefault_ExplicitContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context context = TracingContextUtils.withSpan(span, Context.current());
    assertThat(TracingContextUtils.getSpanWithoutDefault(context)).isSameInstanceAs(span);
  }
}
