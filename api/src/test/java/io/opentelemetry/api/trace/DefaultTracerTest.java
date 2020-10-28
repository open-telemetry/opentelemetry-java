/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link DefaultTracer}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
class DefaultTracerTest {
  private static final Tracer defaultTracer = Tracer.getDefault();
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
  void spanBuilderWithName_NullName() {
    assertThrows(NullPointerException.class, () -> defaultTracer.spanBuilder(null));
  }

  @Test
  void defaultSpanBuilderWithName() {
    assertThat(defaultTracer.spanBuilder(SPAN_NAME).startSpan().getSpanContext().isValid())
        .isFalse();
  }

  @Test
  void testSpanContextPropagationExplicitParent() {
    Span span =
        defaultTracer
            .spanBuilder(SPAN_NAME)
            .setParent(Context.root().with(Span.wrap(spanContext)))
            .startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void testSpanContextPropagation() {
    Span parent = Span.wrap(spanContext);

    Span span =
        defaultTracer.spanBuilder(SPAN_NAME).setParent(Context.root().with(parent)).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void noSpanContextMakesInvalidSpans() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
    assertThat(span.getSpanContext()).isSameAs(SpanContext.getInvalid());
  }

  @Test
  void testSpanContextPropagation_nullContext() {
    assertThrows(
        NullPointerException.class, () -> defaultTracer.spanBuilder(SPAN_NAME).setParent(null));
  }

  @Test
  void testSpanContextPropagation_fromContext() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void testSpanContextPropagation_fromContextAfterNoParent() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void testSpanContextPropagation_fromContextThenNoParent() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).setNoParent().startSpan();
    assertThat(span.getSpanContext()).isEqualTo(SpanContext.getInvalid());
  }
}
