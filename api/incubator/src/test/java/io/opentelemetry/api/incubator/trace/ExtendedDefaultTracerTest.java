/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.testing.internal.AbstractDefaultTracerTest;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExtendedDefaultTracerTest extends AbstractDefaultTracerTest {

  @Override
  public Tracer getTracer() {
    return ExtendedDefaultTracer.getNoop();
  }

  @Override
  public TracerProvider getTracerProvider() {
    return ExtendedDefaultTracerProvider.getNoop();
  }

  @Test
  void incubatingApiIsLoaded() {
    Tracer tracer = TracerProvider.noop().get("test");
    assertThat(tracer).isSameAs(OpenTelemetry.noop().getTracer("test"));

    assertThat(tracer)
        .isInstanceOfSatisfying(
            ExtendedTracer.class,
            extendedTracer -> assertThat(extendedTracer.isEnabled()).isFalse());
    assertThat(tracer.spanBuilder("test")).isInstanceOf(ExtendedSpanBuilder.class);
  }

  @SuppressWarnings("unchecked")
  @Test
  void incubatingApi() {
    ExtendedSpanBuilder spanBuilder =
        (ExtendedSpanBuilder) ExtendedDefaultTracer.getNoop().spanBuilder("test");
    assertThat(spanBuilder.setParentFrom(null, null)).isSameAs(spanBuilder);

    SpanRunnable<RuntimeException> spanRunnable = Mockito.mock(SpanRunnable.class);

    spanBuilder.startAndRun(spanRunnable);
    verify(spanRunnable).runInSpan();
    reset(spanRunnable);

    spanBuilder.startAndRun(spanRunnable, null);
    verify(spanRunnable).runInSpan();
    reset(spanRunnable);

    SpanCallable<String, RuntimeException> spanCallable = Mockito.mock(SpanCallable.class);

    spanBuilder.startAndCall(spanCallable);
    verify(spanCallable).callInSpan();
    reset(spanCallable);

    spanBuilder.startAndCall(spanCallable, null);
    verify(spanCallable).callInSpan();
    reset(spanCallable);
  }
}
