/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import static io.opentelemetry.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.opentelemetry.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.trace.Span.Kind;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link Span.Builder}. */
class SpanBuilderTest {
  private final Tracer tracer = DefaultTracer.getInstance();

  @Test
  void doNotCrash_NoopImplementation() {
    Span.Builder spanBuilder = tracer.spanBuilder("MySpanName");
    spanBuilder.setSpanKind(Kind.SERVER);
    spanBuilder.setParent(TracingContextUtils.withSpan(DefaultSpan.create(null), Context.root()));
    spanBuilder.setParent(Context.root());
    spanBuilder.setNoParent();
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext());
    spanBuilder.addLink(DefaultSpan.getInvalid().getContext(), Attributes.empty());
    spanBuilder.setAttribute("key", "value");
    spanBuilder.setAttribute("key", 12345L);
    spanBuilder.setAttribute("key", .12345);
    spanBuilder.setAttribute("key", true);
    spanBuilder.setAttribute(stringKey("key"), "value");
    spanBuilder.setStartTimestamp(12345L);
    assertThat(spanBuilder.startSpan()).isInstanceOf(DefaultSpan.class);
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
