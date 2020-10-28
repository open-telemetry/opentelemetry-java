/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.context.Context;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Span.Builder}. */
class SpanBuilderTest {
  private final Tracer tracer = Tracer.getDefault();

  @Test
  void doNotCrash_NoopImplementation() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    spanBuilder.setSpanKind(Kind.SERVER);
    spanBuilder.setParent(Context.root().with(Span.wrap(null)));
    spanBuilder.setParent(Context.root());
    spanBuilder.setNoParent();
    spanBuilder.addLink(Span.getInvalid().getSpanContext());
    spanBuilder.addLink(Span.getInvalid().getSpanContext(), Attributes.empty());
    spanBuilder.setAttribute("key", "value");
    spanBuilder.setAttribute("key", 12345L);
    spanBuilder.setAttribute("key", .12345);
    spanBuilder.setAttribute("key", true);
    spanBuilder.setAttribute(stringKey("key"), "value");
    spanBuilder.setStartTimestamp(12345L);
    assertThat(spanBuilder.startSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void setParent_NullContext() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThrows(NullPointerException.class, () -> spanBuilder.setParent(null));
  }

  @Test
  void setStartTimestamp_Negative() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThrows(
        IllegalArgumentException.class,
        () -> spanBuilder.setStartTimestamp(-1),
        "Negative startTimestamp");
  }
}
