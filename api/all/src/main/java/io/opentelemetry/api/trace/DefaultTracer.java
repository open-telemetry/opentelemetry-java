/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.common.ApiUsageLogger;
import io.opentelemetry.context.Context;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/** No-op implementation of {@link Tracer}. */
@ThreadSafe
final class DefaultTracer implements Tracer {

  private static final Tracer INSTANCE = new DefaultTracer();

  static Tracer getInstance() {
    return INSTANCE;
  }

  @Override
  public boolean isEnabled() {
    return false;
  }

  @Override
  public SpanBuilder spanBuilder(String spanName) {
    if (spanName == null) {
      ApiUsageLogger.logNullParam(Tracer.class, "spanBuilder", "spanName");
    }
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
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setParent", "context");
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
      if (spanContext == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "addLink", "spanContext");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
      if (spanContext == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "addLink", "spanContext");
      }
      if (attributes == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "addLink", "attributes");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, @Nullable String value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, long value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, double value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setAttribute(String key, boolean value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public <T> NoopSpanBuilder setAttribute(AttributeKey<T> key, @Nullable T value) {
      if (key == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setAttribute", "key");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setAllAttributes(Attributes attributes) {
      if (attributes == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setAllAttributes", "attributes");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setSpanKind(SpanKind spanKind) {
      if (spanKind == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setSpanKind", "spanKind");
      }
      return this;
    }

    @Override
    public NoopSpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
      if (unit == null) {
        ApiUsageLogger.logNullParam(SpanBuilder.class, "setStartTimestamp", "unit");
      }
      return this;
    }

    private NoopSpanBuilder() {}
  }
}
