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
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.grpc.Context;
import io.opentelemetry.context.ContextUtils;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultTracer}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
class DefaultTracerTest {
  private static final Tracer defaultTracer = DefaultTracer.getInstance();
  private static final String SPAN_NAME = "MySpanName";
  private static final byte[] firstBytes =
      new byte[] {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final byte[] spanBytes = new byte[] {0, 0, 0, 0, 0, 0, 0, 'a'};
  private static final Span propagatedSpan =
      Span.getPropagated(
          TraceId.bytesToHex(firstBytes),
          SpanId.bytesToHex(spanBytes),
          TraceFlags.getDefault(),
          TraceState.getDefault());

  @Test
  void defaultGetCurrentSpan() {
    assertThat(defaultTracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void getCurrentSpan_WithSpan() {
    assertThat(defaultTracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    try (Scope ws = defaultTracer.withSpan(Span.getInvalid())) {
      assertThat(defaultTracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
    }
    assertThat(defaultTracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void spanBuilderWithName_NullName() {
    assertThrows(NullPointerException.class, () -> defaultTracer.spanBuilder(null));
  }

  @Test
  void defaultSpanBuilderWithName() {
    assertThat(defaultTracer.spanBuilder(SPAN_NAME).startSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void testInProcessContext() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
    try (Scope scope = defaultTracer.withSpan(span)) {
      assertThat(defaultTracer.getCurrentSpan()).isEqualTo(span);
      Span secondSpan = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      try (Scope secondScope = defaultTracer.withSpan(secondSpan)) {
        assertThat(defaultTracer.getCurrentSpan()).isEqualTo(secondSpan);
      } finally {
        assertThat(defaultTracer.getCurrentSpan()).isEqualTo(span);
      }
    }
    assertThat(defaultTracer.getCurrentSpan()).isInstanceOf(DefaultSpan.class);
  }

  @Test
  void testSpanContextPropagationExplicitParent() {
    Span span =
        defaultTracer
            .spanBuilder(SPAN_NAME)
            .setParent(TracingContextUtils.withSpan(propagatedSpan, Context.ROOT))
            .startSpan();
    assertThat(span).isSameAs(propagatedSpan);
  }

  @Test
  void testSpanContextPropagation() {
    Span parent = propagatedSpan;

    Span span =
        defaultTracer
            .spanBuilder(SPAN_NAME)
            .setParent(TracingContextUtils.withSpan(parent, Context.ROOT))
            .startSpan();
    assertThat(span).isSameAs(propagatedSpan);
  }

  @Test
  void noSpanContextMakesInvalidSpans() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
    assertThat(span).isSameAs(Span.getInvalid());
  }

  @Test
  void testSpanContextPropagation_nullContext() {
    assertThrows(
        NullPointerException.class,
        () -> defaultTracer.spanBuilder(SPAN_NAME).setParent((Context) null));
  }

  @Test
  void testSpanContextPropagation_fromContext() {
    Context context = TracingContextUtils.withSpan(propagatedSpan, Context.current());

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).startSpan();
    assertThat(span).isSameAs(propagatedSpan);
  }

  @Test
  void testSpanContextPropagation_fromContextAfterNoParent() {
    Context context = TracingContextUtils.withSpan(propagatedSpan, Context.current());

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
    assertThat(span).isSameAs(propagatedSpan);
  }

  @Test
  void testSpanContextPropagation_fromContextThenNoParent() {
    Context context = TracingContextUtils.withSpan(propagatedSpan, Context.current());

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).setNoParent().startSpan();
    assertThat(span).isEqualTo(Span.getInvalid());
  }

  @Test
  void testSpanContextPropagationCurrentSpan() {
    Span parent = propagatedSpan;
    try (Scope scope = defaultTracer.withSpan(parent)) {
      Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span).isSameAs(propagatedSpan);
    }
  }

  @Test
  void testSpanContextPropagationCurrentSpanContext() {
    Context context = TracingContextUtils.withSpan(propagatedSpan, Context.current());
    try (Scope scope = ContextUtils.withScopedContext(context)) {
      Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span).isSameAs(propagatedSpan);
    }
  }
}
