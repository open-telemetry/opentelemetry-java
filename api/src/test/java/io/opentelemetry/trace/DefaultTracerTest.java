/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.context.Context;
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
  private static final SpanContext spanContext =
      SpanContext.create(
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
    try (Scope ws = defaultTracer.withSpan(DefaultSpan.getInvalid())) {
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
            .setParent(
                TracingContextUtils.withSpan(DefaultSpan.create(spanContext), Context.root()))
            .startSpan();
    assertThat(span.getContext()).isSameAs(spanContext);
  }

  @Test
  void testSpanContextPropagation() {
    DefaultSpan parent = new DefaultSpan(spanContext);

    Span span =
        defaultTracer
            .spanBuilder(SPAN_NAME)
            .setParent(TracingContextUtils.withSpan(parent, Context.root()))
            .startSpan();
    assertThat(span.getContext()).isSameAs(spanContext);
  }

  @Test
  void noSpanContextMakesInvalidSpans() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
    assertThat(span.getContext()).isSameAs(SpanContext.getInvalid());
  }

  @Test
  void testSpanContextPropagation_nullContext() {
    assertThrows(
        NullPointerException.class,
        () -> defaultTracer.spanBuilder(SPAN_NAME).setParent((Context) null));
  }

  @Test
  void testSpanContextPropagation_fromContext() {
    Context context = TracingContextUtils.withSpan(new DefaultSpan(spanContext), Context.current());

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).startSpan();
    assertThat(span.getContext()).isSameAs(spanContext);
  }

  @Test
  void testSpanContextPropagation_fromContextAfterNoParent() {
    Context context = TracingContextUtils.withSpan(new DefaultSpan(spanContext), Context.current());

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
    assertThat(span.getContext()).isSameAs(spanContext);
  }

  @Test
  void testSpanContextPropagation_fromContextThenNoParent() {
    Context context = TracingContextUtils.withSpan(new DefaultSpan(spanContext), Context.current());

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).setNoParent().startSpan();
    assertThat(span.getContext()).isEqualTo(SpanContext.getInvalid());
  }

  @Test
  void testSpanContextPropagationCurrentSpan() {
    DefaultSpan parent = new DefaultSpan(spanContext);
    try (Scope scope = defaultTracer.withSpan(parent)) {
      Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span.getContext()).isSameAs(spanContext);
    }
  }

  @Test
  void testSpanContextPropagationCurrentSpanContext() {
    Context context =
        TracingContextUtils.withSpan(DefaultSpan.create(spanContext), Context.current());
    try (Scope scope = context.makeCurrent()) {
      Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
      assertThat(span.getContext()).isSameAs(spanContext);
    }
  }
}
