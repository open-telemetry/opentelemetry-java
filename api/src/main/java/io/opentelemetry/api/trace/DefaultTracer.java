/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.internal.Utils;
import io.opentelemetry.context.Context;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** No-op implementations of {@link Tracer}. */
@ThreadSafe
final class DefaultTracer implements Tracer {

  private static final DefaultTracer INSTANCE = new DefaultTracer();

  static DefaultTracer getInstance() {
    return INSTANCE;
  }

  @Override
  public SpanBuilder spanBuilder(String spanName) {
    return NoopSpanBuilder.create(spanName);
  }

  private DefaultTracer() {}

  // Noop implementation of Span.Builder.
  private static final class NoopSpanBuilder implements SpanBuilder {
    static NoopSpanBuilder create(String spanName) {
      return new NoopSpanBuilder(spanName);
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
      Objects.requireNonNull(context, "context");
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
    public NoopSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
      Utils.checkArgument(startTimestamp >= 0, "Negative startTimestamp");
      return this;
    }

    private NoopSpanBuilder(String name) {
      Objects.requireNonNull(name, "name");
    }
  }
}
