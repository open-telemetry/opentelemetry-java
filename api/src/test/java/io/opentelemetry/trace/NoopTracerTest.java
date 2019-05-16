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

import io.opentelemetry.context.Scope;
import io.opentelemetry.context.propagation.TraceContextFormat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit tests for {@link NoopTracer}. */
@RunWith(JUnit4.class)
// Need to suppress warnings for MustBeClosed because Java-6 does not support try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public class NoopTracerTest {
  private static final Tracer noopTracer = NoopTracer.create();
  private static final String SPAN_NAME = "MySpanName";
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final SpanContext spanContext =
      SpanContext.create(
          TraceId.fromBytes(firstBytes, 0),
          SpanId.fromBytes(firstBytes, 8),
          TraceOptions.DEFAULT,
          Tracestate.builder().build());

  @Test
  public void defaultGetCurrentSpan() {
    assertThat(noopTracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test
  public void getCurrentSpan_WithSpan() {
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    Scope ws = noopTracer.withSpan(BlankSpan.INSTANCE);
    try {
      assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
    } finally {
      ws.close();
    }
    assertThat(noopTracer.getCurrentSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithName_NullName() {
    noopTracer.spanBuilder(null);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(noopTracer.spanBuilder(SPAN_NAME).startSpan()).isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithParentAndName_NullName() {
    noopTracer.spanBuilderWithExplicitParent(null, null);
  }

  @Test
  public void defaultSpanBuilderWithParentAndName() {
    assertThat(noopTracer.spanBuilderWithExplicitParent(SPAN_NAME, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test(expected = NullPointerException.class)
  public void spanBuilderWithRemoteParent_NullName() {
    noopTracer.spanBuilderWithRemoteParent(null, null);
  }

  @Test
  public void defaultSpanBuilderWithRemoteParent_NullParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, null).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultSpanBuilderWithRemoteParent() {
    assertThat(noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, SpanContext.BLANK).startSpan())
        .isSameAs(BlankSpan.INSTANCE);
  }

  @Test
  public void defaultHttpTextFormat() {
    assertThat(noopTracer.getHttpTextFormat()).isInstanceOf(TraceContextFormat.class);
  }

  @Test
  public void testInProcessContext() {
    Span span = noopTracer.spanBuilder(SPAN_NAME).startSpan();
    Scope scope = noopTracer.withSpan(span);
    try {
      assertThat(noopTracer.getCurrentSpan()).isEqualTo(span);
      Span secondSpan = noopTracer.spanBuilder(SPAN_NAME).startSpan();
      Scope secondScope = noopTracer.withSpan(span);
      try {
        assertThat(noopTracer.getCurrentSpan()).isEqualTo(secondSpan);
        secondScope.close();
        assertThat(noopTracer.getCurrentSpan()).isEqualTo(span);
      } finally {
        secondScope.close();
      }
    } finally {
      scope.close();
    }
    assertThat(noopTracer.getCurrentSpan()).isEqualTo(BlankSpan.INSTANCE);
  }

  @Test
  public void testSpanContextPropagationExplicitParent() {
    Span span = noopTracer.spanBuilderWithRemoteParent(SPAN_NAME, spanContext).startSpan();
    assertThat(span.getContext()).isSameInstanceAs(spanContext);
  }

  @Test
  public void testSpanContextPropagation() {
    BlankSpan parent = new BlankSpan(spanContext);

    Span span = noopTracer.spanBuilderWithExplicitParent(SPAN_NAME, parent).startSpan();
    assertThat(span.getContext()).isSameInstanceAs(spanContext);
  }

  @Test
  public void testSpanContextPropagationCurrentSpan() {
    BlankSpan parent = new BlankSpan(spanContext);
    Scope scope = noopTracer.withSpan(parent);
    try {
      Span span = noopTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span.getContext()).isSameInstanceAs(spanContext);
    } finally {
      scope.close();
    }
  }
}
