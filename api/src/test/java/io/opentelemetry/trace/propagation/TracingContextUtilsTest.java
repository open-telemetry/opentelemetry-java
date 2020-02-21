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

package io.opentelemetry.trace.propagation;

import static com.google.common.truth.Truth.assertThat;

import io.grpc.Context;
import io.opentelemetry.OpenTelemetry;
import io.opentelemetry.trace.DefaultSpan;
import io.opentelemetry.trace.Span;
import io.opentelemetry.trace.SpanContext;
import io.opentelemetry.trace.SpanId;
import io.opentelemetry.trace.TraceFlags;
import io.opentelemetry.trace.TraceId;
import io.opentelemetry.trace.TraceState;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link TracingContextUtils}. */
@RunWith(JUnit4.class)
public final class TracingContextUtilsTest {
  @Test
  public void testGetCurrentSpan_DefaultContext() {
    Span span = TracingContextUtils.getSpan(Context.current());
    assertThat(span).isNull();
  }

  @Test
  public void testGetCurrentSpan_DefaultContext_WithoutExplicitContext() {
    Span span = TracingContextUtils.getSpan();
    assertThat(span).isNull();
  }

  @Test
  public void testGetCurrentSpanWithDefault_DefaultContext() {
    Span span = TracingContextUtils.getSpanWithDefault(Context.current());
    assertThat(span).isNotNull();
    assertThat(span).isSameInstanceAs(DefaultSpan.getInvalid());
  }

  @Test
  public void testGetCurrentSpanWithDefault_ContextSetToNull() {
    Context orig = TracingContextUtils.withSpan(null, Context.current()).attach();
    try {
      Span span = TracingContextUtils.getSpanWithDefault(Context.current());
      assertThat(span).isNotNull();
      assertThat(span).isSameInstanceAs(DefaultSpan.getInvalid());
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testGetCurrentSpanContext_DefaultContext() {
    SpanContext spanContext = TracingContextUtils.getSpanContext(Context.current());
    assertThat(spanContext).isNull();
  }

  @Test
  public void testGetCurrentSpanContext_DefaultContext_WithoutExplicitContext() {
    SpanContext spanContext = TracingContextUtils.getSpanContext();
    assertThat(spanContext).isNull();
  }

  @Test
  public void testAnySpanContext_DefaultContext() {
    SpanContext spanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
    assertThat(spanContext).isNull();
  }

  @Test
  public void testAnySpanContext_SetSpanContext() {
    Context orig =
        TracingContextUtils.withSpanContext(DefaultSpan.getInvalid().getContext()).attach();
    try {
      SpanContext spanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
      assertThat(spanContext).isNotNull();
      assertThat(spanContext).isSameInstanceAs(DefaultSpan.getInvalid().getContext());
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testAnySpanContext_SetSpan() {
    Context orig = TracingContextUtils.withSpan(DefaultSpan.getInvalid()).attach();
    try {
      SpanContext spanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
      assertThat(spanContext).isNotNull();
      assertThat(spanContext).isSameInstanceAs(DefaultSpan.getInvalid().getContext());
    } finally {
      Context.current().detach(orig);
    }
  }

  @Test
  public void testSetSpanAndSpanContext() {
    Span span = OpenTelemetry.getTracerRegistry().get(null).spanBuilder("testSpan").startSpan();
    SpanContext spanContext =
        SpanContext.create(
            new TraceId(1, 1), new SpanId(1), TraceFlags.getDefault(), TraceState.getDefault());

    Context context1 = TracingContextUtils.withSpan(span).attach();
    try {
      Context context2 = TracingContextUtils.withSpanContext(spanContext).attach();
      try {
        SpanContext spanContextInContext = TracingContextUtils.getSpanContext(Context.current());
        assertThat(spanContextInContext).isNotNull();
        assertThat(spanContextInContext).isSameInstanceAs(spanContext);

        Span spanInContext = TracingContextUtils.getSpan(Context.current());
        assertThat(spanInContext).isNotNull();
        assertThat(spanInContext).isSameInstanceAs(span);

        SpanContext anySpanContext = TracingContextUtils.getEffectiveSpanContext(Context.current());
        assertThat(anySpanContext).isNotNull();
        assertThat(anySpanContext).isSameInstanceAs(span.getContext());
      } finally {
        Context.current().detach(context2);
      }
    } finally {
      Context.current().detach(context1);
    }
  }
}
