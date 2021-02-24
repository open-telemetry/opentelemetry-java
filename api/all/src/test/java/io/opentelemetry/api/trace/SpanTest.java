/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;

import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import org.junit.jupiter.api.Test;

class SpanTest {
  @Test
  void testGetCurrentSpan_Default() {
    Span span = Span.current();
    assertThat(span).isSameAs(Span.getInvalid());
  }

  @Test
  void testGetCurrentSpan_SetSpan() {
    Span span = Span.wrap(SpanContext.getInvalid());
    try (Scope ignored = Context.current().with(span).makeCurrent()) {
      assertThat(Span.current()).isSameAs(span);
    }
  }

  @Test
  void testGetSpan_DefaultContext() {
    Span span = Span.fromContext(Context.root());
    assertThat(span).isSameAs(Span.getInvalid());
  }

  @Test
  void testGetSpan_ExplicitContext() {
    Span span = Span.wrap(SpanContext.getInvalid());
    Context context = Context.root().with(span);
    assertThat(Span.fromContext(context)).isSameAs(span);
  }

  @Test
  void testGetSpanWithoutDefault_DefaultContext() {
    Span span = Span.fromContextOrNull(Context.root());
    assertThat(span).isNull();
  }

  @Test
  void testGetSpanWithoutDefault_ExplicitContext() {
    Span span = Span.wrap(SpanContext.getInvalid());
    Context context = Context.root().with(span);
    assertThat(Span.fromContextOrNull(context)).isSameAs(span);
  }

  @Test
  void testInProcessContext() {
    Span span = Span.wrap(SpanContext.getInvalid());
    try (Scope scope = span.makeCurrent()) {
      assertThat(Span.current()).isSameAs(span);
      Span secondSpan = Span.wrap(SpanContext.getInvalid());
      try (Scope secondScope = secondSpan.makeCurrent()) {
        assertThat(Span.current()).isSameAs(secondSpan);
      } finally {
        assertThat(Span.current()).isSameAs(span);
      }
    }
    assertThat(Span.current().getSpanContext().isValid()).isFalse();
  }

  @Test
  void fromContext_null() {
    assertThat(Span.fromContext(null)).isEqualTo(Span.getInvalid());
    assertThat(Span.fromContextOrNull(null)).isNull();
  }
}
