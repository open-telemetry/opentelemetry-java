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
    Span span = TracingContextUtils.getSpan();
    assertThat(span).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  @Test
  public void testGetCurrentSpan_Default_SetSpan() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context orig = TracingContextUtils.withSpan(span).attach();
    try {
      assertThat(TracingContextUtils.getSpan()).isSameInstanceAs(span);
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testGetCurrentSpan_DefaultContext() {
    Span span = TracingContextUtils.getSpan(Context.current());
    assertThat(span).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  @Test
  public void testGetCurrentSpan_ExplicitContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context context = TracingContextUtils.withSpan(span);
    assertThat(TracingContextUtils.getSpan(context)).isSameInstanceAs(span);
  }

  @Test
  public void testGetCurrentSpanWithoutDefault_DefaultContext() {
    Span span = TracingContextUtils.getSpanWithoutDefault(Context.current());
    assertThat(span).isNull();
  }

  @Test
  public void testGetCurrentSpanWithoutDefault_ExplicitContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    Context context = TracingContextUtils.withSpan(span);
    assertThat(TracingContextUtils.getSpanWithoutDefault(context)).isSameInstanceAs(span);
  }

  @Test
  public void testGetCurrentSpanContextWithoutDefault_DefaultContext() {
    SpanContext spanContext = TracingContextUtils.getSpanContext(Context.current());
    assertThat(spanContext).isNull();
  }

  @Test
  public void testGetCurrentSpanContext_DefaultContext_WithoutExplicitContext() {
    SpanContext spanContext = TracingContextUtils.getSpanContext();
    assertThat(spanContext).isNull();
  }

  @Test
  public void testEffectiveSpanContext_DefaultContext() {
    SpanContext spanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
    assertThat(spanContext).isNull();
  }

  @Test
  public void testEffectiveSpanContext_SetSpanContext() {
    Context orig =
        TracingContextUtils.withSpanContext(DefaultSpan.getInvalid().getContext()).attach();
    try {
      SpanContext spanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
      assertThat(spanContext).isSameInstanceAs(DefaultSpan.getInvalid().getContext());
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testEffectiveSpanContext_SetSpan() {
    Context orig = TracingContextUtils.withSpan(DefaultSpan.getInvalid()).attach();
    try {
      SpanContext spanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
      assertThat(spanContext).isSameInstanceAs(DefaultSpan.getInvalid().getContext());
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testSetSpanAndSpanContext() {
    Span span = DefaultSpan.create(SpanContext.getInvalid());
    SpanContext spanContext =
        SpanContext.create(
            new TraceId(1, 1), new SpanId(1), TraceFlags.getDefault(), TraceState.getDefault());

    Context context1 = TracingContextUtils.withSpan(span).attach();
    try {
      Context context2 = TracingContextUtils.withSpanContext(spanContext).attach();
      try {
        SpanContext spanContextInContext = TracingContextUtils.getSpanContext(Context.current());
        assertThat(spanContextInContext).isSameInstanceAs(spanContext);

        Span spanInContext = TracingContextUtils.getSpanWithoutDefault(Context.current());
        assertThat(spanInContext).isSameInstanceAs(span);

        SpanContext anySpanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
        assertThat(anySpanContext).isSameInstanceAs(span.getContext());
      } finally {
        Context.current().detach(context2);
      }
    } finally {
      Context.current().detach(context1);
    }
  }
}
