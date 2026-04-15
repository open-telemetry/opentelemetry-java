/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.propagation.ExtendedContextPropagators;
import io.opentelemetry.api.internal.ApiUsageLogger;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** No-op implementation of {@link ExtendedTracer}. */
@ThreadSafe
final class ExtendedDefaultTracer implements ExtendedTracer {

  private static final Tracer INSTANCE = new ExtendedDefaultTracer();

  static Tracer getNoop() {
    return INSTANCE;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public ExtendedSpanBuilder spanBuilder(String spanName) {
    return NoopSpanBuilder.create();
  }

  private ExtendedDefaultTracer() {}

  // Noop implementation of Span.Builder.
  private static final class NoopSpanBuilder implements ExtendedSpanBuilder {
    static NoopSpanBuilder create() {
      return new NoopSpanBuilder();
    }

    @Nullable private SpanContext spanContext;

    @Override
    public Span startSpan() {
      if (spanContext == null) {
        spanContext = Span.current().getSpanContext();
      }

      return Span.wrap(spanContext);
    }

    @Override
    public NoopSpanBuilder setParent(Context context) {
      if (context == null) {
        ApiUsageLogger.log("context is null");
        return this;
      }
      spanContext = Span.fromContext(context).getSpanContext();
      return this;
    }

    @Override
    public NoopSpanBuilder setParentFrom(
        ContextPropagators propagators, Map<String, String> carrier) {
      setParent(ExtendedContextPropagators.extractTextMapPropagationContext(carrier, propagators));
      return this;
    }

    @Override
    public NoopSpanBuilder setNoParent() {
      spanContext = SpanContext.getInvalid();
      return this;
    }

    @Override
    public NoopSpanBuilder addLink(SpanContext spanContext) {
      return this;
    }

    @Override
    public NoopSpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, @Nullable String value) {
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, long value) {
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, double value) {
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, boolean value) {
      return this;
    }

    @Override
    public <T> NoopSpanBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
      return this;
    }

    @Override
    public NoopSpanBuilder setAllAttributes(Attributes attributes) {
      return this;
    }

    @Override
    public NoopSpanBuilder setSpanKind(SpanKind spanKind) {
      return this;
    }

    @Override
    public NoopSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public <T, E extends Throwable> T startAndCall(SpanCallable<T, E> spanCallable) throws E {
      return spanCallable.callInSpan();
    }

    @Override
    public <T, E extends Throwable> T startAndCall(
        SpanCallable<T, E> spanCallable, BiConsumer<Span, Throwable> handleException) throws E {
      return spanCallable.callInSpan();
    }

    @Override
    public <E extends Throwable> void startAndRun(SpanRunnable<E> runnable) throws E {
      runnable.runInSpan();
    }

    @Override
    public <E extends Throwable> void startAndRun(
        SpanRunnable<E> runnable, BiConsumer<Span, Throwable> handleException) throws E {
      runnable.runInSpan();
    }

    private NoopSpanBuilder() {}
  }
}
