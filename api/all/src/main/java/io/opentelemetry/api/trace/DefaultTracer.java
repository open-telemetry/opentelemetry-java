/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** No-op implementations of {@link Tracer}. */
@ThreadSafe
final class DefaultTracer implements Tracer {

  private static final Tracer INSTANCE = new DefaultTracer();

  static Tracer getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanBuilder spanBuilder(String spanName) {
    return NoopSpanBuilder.create();
  }

  private DefaultTracer() {}

  // Noop implementation of Span.Builder.
  private static final class NoopSpanBuilder implements SpanBuilder {
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
        return this;
      }
      spanContext = Span.fromContext(context).getSpanContext();
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

    private NoopSpanBuilder() {}
  }
}
