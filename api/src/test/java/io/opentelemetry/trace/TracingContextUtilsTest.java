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

import static org.assertj.core.api.Assertions.assertThat;

import io.grpc.Context;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link TracingContextUtils}. */
public final class TracingContextUtilsTest {

  @Test
  void testGetCurrentSpan_Default() {
    Span span = TracingContextUtils.getCurrentSpan();
    assertThat(span).isSameAs(DefaultSpan.getInvalid());
  }

  @Test
  void testGetCurrentSpan_SetSpan() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context orig = TracingContextUtils.withSpan(span, Context.current()).attach();
    try {
      assertThat(TracingContextUtils.getCurrentSpan()).isSameAs(span);
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  void testGetSpan_DefaultContext() {
    Span span = TracingContextUtils.getSpan(Context.current());
    assertThat(span).isSameAs(DefaultSpan.getInvalid());
  }

  @Test
  void testGetSpan_ExplicitContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context context = TracingContextUtils.withSpan(span, Context.current());
    assertThat(TracingContextUtils.getSpan(context)).isSameAs(span);
  }

  @Test
  void testGetSpanWithoutDefault_DefaultContext() {
    Span span = TracingContextUtils.getSpanWithoutDefault(Context.current());
    assertThat(span).isNull();
  }

  @Test
  void testGetSpanWithoutDefault_ExplicitContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context context = TracingContextUtils.withSpan(span, Context.current());
    assertThat(TracingContextUtils.getSpanWithoutDefault(context)).isSameAs(span);
  }
}
