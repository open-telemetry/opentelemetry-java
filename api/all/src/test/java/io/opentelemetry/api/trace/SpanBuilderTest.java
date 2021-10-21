/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for {@link SpanBuilder}. */
class SpanBuilderTest {
  private final Tracer tracer = DefaultTracer.getInstance();

  @Test
  void doNotCrash_NoopImplementation() {
    assertThatCode(
            () -> {
              SpanBuilder spanBuilder = tracer.spanBuilder(null);
              spanBuilder.setSpanKind(null);
              spanBuilder.setParent(null);
              spanBuilder.setNoParent();
              spanBuilder.addLink(null);
              spanBuilder.addLink(null, Attributes.empty());
              spanBuilder.addLink(SpanContext.getInvalid(), null);
              spanBuilder.setAttribute((String) null, "foo");
              spanBuilder.setAttribute("foo", null);
              spanBuilder.setAttribute(null, 0L);
              spanBuilder.setAttribute(null, 0.0);
              spanBuilder.setAttribute(null, false);
              spanBuilder.setAttribute((AttributeKey<String>) null, "foo");
              spanBuilder.setAttribute(stringKey(null), "foo");
              spanBuilder.setAttribute(stringKey(""), "foo");
              spanBuilder.setAttribute(stringKey("foo"), null);
              spanBuilder.setStartTimestamp(-1, TimeUnit.MILLISECONDS);
              spanBuilder.setStartTimestamp(1, null);
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
              spanBuilder.setAllAttributes(Attributes.of(stringKey("key"), "value"));
              spanBuilder.setAllAttributes(Attributes.empty());
              spanBuilder.setAllAttributes(null);
              spanBuilder.setStartTimestamp(12345L, TimeUnit.NANOSECONDS);
              spanBuilder.setStartTimestamp(Instant.EPOCH);
              spanBuilder.setStartTimestamp(null);
              assertThat(spanBuilder.startSpan().getSpanContext().isValid()).isFalse();
            })
        .doesNotThrowAnyException();
  }
}
