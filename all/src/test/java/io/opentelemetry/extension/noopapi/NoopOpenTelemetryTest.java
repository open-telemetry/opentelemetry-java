/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.noopapi;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class NoopOpenTelemetryTest {

  private static final SpanContext SPAN_CONTEXT =
      SpanContext.create(
          "00000000000000000000000000000061",
          "0000000000000061",
          TraceFlags.getDefault(),
          TraceState.getDefault());

  @Test
  void contextNoOp() {
    // Context.root() is not a no-op Context, so the default context is never root.
    Context context = Context.current();
    assertThat(context).isNotSameAs(Context.groot());
    // No allocations
    assertThat(context.with(Span.wrap(SPAN_CONTEXT))).isSameAs(context);
    assertThat(SPAN_CONTEXT.isValid()).isTrue();
    try (Scope ignored = Context.current().with(Span.wrap(SPAN_CONTEXT)).makeCurrent()) {
      // No context mounted, so always an invalid span.
      assertThat(Span.fromContext(Context.current()).getSpanContext().isValid()).isFalse();
    }
  }

  @Test
  void tracerNoOp() {
    SpanBuilder span1 = NoopOpenTelemetry.getInstance().getTracer("test").spanBuilder("test");
    SpanBuilder span2 = NoopOpenTelemetry.getInstance().getTracer("test").spanBuilder("test");
    // No allocations
    assertThat(span1).isSameAs(span2);

    // No crash
    span1.setParent(Context.current());
    span1.setNoParent();
    span1.addLink(SPAN_CONTEXT);
    span1.addLink(SPAN_CONTEXT, Attributes.empty());
    span1.setAttribute("key", "value");
    span1.setAttribute("key", 1L);
    span1.setAttribute("key", 1.0);
    span1.setAttribute("key", true);
    span1.setAttribute(AttributeKey.stringKey("key"), "value");
    span1.setSpanKind(SpanKind.CLIENT);
    span1.setStartTimestamp(1, TimeUnit.DAYS);

    // No allocations
    assertThat(span1.startSpan()).isSameAs(Span.getInvalid());
  }
}
