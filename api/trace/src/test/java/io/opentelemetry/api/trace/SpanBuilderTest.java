/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span.Kind;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanBuilder}. */
class SpanBuilderTest {
  private final Tracer tracer = Tracer.getDefault();

  @Test
  void doNotCrash_NoopImplementation() {
    SpanBuilder spanBuilder = tracer.spanBuilder("MySpanName");
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
    spanBuilder.setStartTimestamp(12345L, TimeUnit.NANOSECONDS);
    spanBuilder.setStartTimestamp(Instant.EPOCH);
    assertThat(spanBuilder.startSpan().getSpanContext().isValid()).isFalse();
  }

  @Test
  void setParent_NullContext() {
    SpanBuilder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThatThrownBy(() -> spanBuilder.setParent(null)).isInstanceOf(NullPointerException.class);
  }

  @Test
  void setStartTimestamp_Negative() {
    SpanBuilder spanBuilder = tracer.spanBuilder("MySpanName");
    assertThatThrownBy(() -> spanBuilder.setStartTimestamp(-1, TimeUnit.NANOSECONDS))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Negative startTimestamp");
  }
}
