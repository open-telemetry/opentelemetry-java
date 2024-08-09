/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import org.junit.Test;

/** Unit tests for {@link DefaultTracer}. */
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
  public void returnsDefaultTracer() {
    TracerProvider tracerProvider = getTracerProvider();
    assertThat(
            tracerProvider
                .tracerBuilder("test")
                .setSchemaUrl("schema")
                .setInstrumentationVersion("1")
                .build())
        .isInstanceOf(DefaultTracer.class);
    assertThat(tracerProvider.get("test")).isInstanceOf(DefaultTracer.class);
    assertThat(tracerProvider.get("test", "1.0")).isInstanceOf(DefaultTracer.class);
  }

  @Test
  public void defaultSpanBuilderWithName() {
    assertThat(defaultTracer.spanBuilder(SPAN_NAME).startSpan().getSpanContext().isValid())
        .isFalse();
  }

  @Test
  public void spanContextPropagationExplicitParent() {
    Span span =
        defaultTracer
            .spanBuilder(SPAN_NAME)
            .setParent(Context.root().with(Span.wrap(spanContext)))
            .startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  public void spanContextPropagation() {
    Span parent = Span.wrap(spanContext);

    Span span =
        defaultTracer.spanBuilder(SPAN_NAME).setParent(Context.root().with(parent)).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  public void noSpanContextMakesInvalidSpans() {
    Span span = defaultTracer.spanBuilder(SPAN_NAME).startSpan();
    assertThat(span.getSpanContext()).isSameAs(SpanContext.getInvalid());
  }

  @Test
  public void spanContextPropagation_fromContext() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  public void spanContextPropagation_fromContextAfterNoParent() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setNoParent().setParent(context).startSpan();
    assertThat(span.getSpanContext()).isSameAs(spanContext);
  }

  @Test
  public void spanContextPropagation_fromContextThenNoParent() {
    Context context = Context.current().with(Span.wrap(spanContext));

    Span span = defaultTracer.spanBuilder(SPAN_NAME).setParent(context).setNoParent().startSpan();
    assertThat(span.getSpanContext()).isEqualTo(SpanContext.getInvalid());
  }

  @Test
  public void addLink() {
    Span span = Span.fromContext(Context.root());
    assertThatCode(() -> span.addLink(null)).doesNotThrowAnyException();
    assertThatCode(() -> span.addLink(SpanContext.getInvalid())).doesNotThrowAnyException();
    assertThatCode(() -> span.addLink(null, null)).doesNotThrowAnyException();
    assertThatCode(() -> span.addLink(SpanContext.getInvalid(), Attributes.empty()))
        .doesNotThrowAnyException();
  }
}
