/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.testing.internal;

import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

/** Unit tests for No-op {@link Tracer}. */
// Need to suppress warnings for MustBeClosed because Android 14 does not support
// try-with-resources.
@SuppressWarnings("MustBeClosedChecker")
public abstract class AbstractDefaultTracerTest {
  private final Tracer defaultTracer = getTracer();
  private static final String SPAN_NAME = "MySpanName";
  private static final SpanContext spanContext =
      SpanContext.create(
          "00000000000000000000000000000061",
          "0000000000000061",
          TraceFlags.getDefault(),
          TraceState.getDefault());

  public abstract Tracer getTracer();

  public abstract TracerProvider getTracerProvider();

  @Test
  void returnsDefaultTracer() {
    TracerProvider tracerProvider = getTracerProvider();
    Class<? extends Tracer> want = defaultTracer.getClass();
    assertThat(
            tracerProvider
                .tracerBuilder("test")
                .setSchemaUrl("schema")
                .setInstrumentationVersion("1")
                .build())
        .isInstanceOf(want);
    assertThat(tracerProvider.get("test")).isInstanceOf(want);
    assertThat(tracerProvider.get("test", "1.0")).isInstanceOf(want);
  }

  @Test
  void defaultSpanBuilderWithName() {
    assertThat(defaultTracer.spanBuilder(SPAN_NAME).startSpan().getSpanContext().isValid())
        .isFalse();
  }

  @Test
  @SuppressWarnings("NullAway")
  void spanContextPropagationExplicitParent() {
    assertThat(
            defaultTracer
                .spanBuilder(SPAN_NAME)
                .setParent(Context.root().with(Span.wrap(spanContext)))
                .startSpan()
                .getSpanContext())
        .isSameAs(spanContext);

    SpanBuilder builder = defaultTracer.spanBuilder(SPAN_NAME);
    assertThat(builder.setParent(null)).isSameAs(builder);
  }

  @Test
  void spanContextPropagation() {
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
  void spanContextPropagation_fromContext() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void spanContextPropagation_fromContextAfterNoParent() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  void spanContextPropagation_fromContextThenNoParent() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).setNoParent().startSpan();
    assertThat(span.getSpanContext()).isEqualTo(SpanContext.getInvalid());
  }

  @Test
  @SuppressWarnings("NullAway")
  void doNotCrash_NoopImplementation() {
    assertThatCode(
            () -> {
              SpanBuilder spanBuilder = defaultTracer.spanBuilder(null);
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
              spanBuilder.setAttribute(longKey("MyLongAttributeKey"), 123);
              assertThat(spanBuilder.startSpan().getSpanContext().isValid()).isFalse();
            })
        .doesNotThrowAnyException();
  }
}
