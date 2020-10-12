/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import io.opentelemetry.internal.Utils;
import java.util.Objects;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * No-op implementations of {@link Tracer}.
 *
 * @since 0.1.0
 */
@ThreadSafe
public final class DefaultTracer implements Tracer {
  private static final DefaultTracer INSTANCE = new DefaultTracer();

  /**
   * Returns a {@code Tracer} singleton that is the default implementations for {@link Tracer}.
   *
   * @return a {@code Tracer} singleton that is the default implementations for {@link Tracer}.
   * @since 0.1.0
   */
  public static Tracer getInstance() {
    return INSTANCE;
  }

  @Override
  public Span getCurrentSpan() {
    return TracingContextUtils.getCurrentSpan();
  }

  @Override
  public Scope withSpan(Span span) {
    return TracingContextUtils.currentContextWith(span);
  }

  @Override
  public Span.Builder spanBuilder(String spanName) {
    return NoopSpanBuilder.create(spanName);
  }

  private DefaultTracer() {}

  // Noop implementation of Span.Builder.
  private static final class NoopSpanBuilder implements Span.Builder {
    static NoopSpanBuilder create(String spanName) {
      return new NoopSpanBuilder(spanName);
    }

    @Nullable private SpanContext spanContext;

    @Override
    public Span startSpan() {
      if (spanContext == null) {
        spanContext = TracingContextUtils.getCurrentSpan().getContext();
      }

      return spanContext != null && !SpanContext.getInvalid().equals(spanContext)
          ? new DefaultSpan(spanContext)
          : DefaultSpan.getInvalid();
    }

    @Override
    public NoopSpanBuilder setParent(Context context) {
      Objects.requireNonNull(context, "context");
      spanContext = TracingContextUtils.getSpan(context).getContext();
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
    public NoopSpanBuilder setAttribute(String key, String value) {
      Objects.requireNonNull(key, "key");
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, long value) {
      Objects.requireNonNull(key, "key");
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, double value) {
      Objects.requireNonNull(key, "key");
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, boolean value) {
      Objects.requireNonNull(key, "key");
      return this;
    }

    @Override
    public <T> NoopSpanBuilder setAttribute(AttributeKey<T> key, T value) {
      Objects.requireNonNull(key, "key");
      Objects.requireNonNull(value, "value");
      return this;
    }

    @Override
    public NoopSpanBuilder setSpanKind(Span.Kind spanKind) {
      return this;
    }

    @Override
    public NoopSpanBuilder setStartTimestamp(long startTimestamp) {
      Utils.checkArgument(startTimestamp >= 0, "Negative startTimestamp");
      return this;
    }

    private NoopSpanBuilder(String name) {
      Objects.requireNonNull(name, "name");
    }
  }
}
