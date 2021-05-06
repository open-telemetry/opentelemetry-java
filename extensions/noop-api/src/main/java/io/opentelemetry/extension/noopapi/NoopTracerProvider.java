/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.noopapi;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.SpanKind;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.TracerProvider;
import io.opentelemetry.context.Context;
import java.util.concurrent.TimeUnit;

enum NoopTracerProvider implements TracerProvider {
  INSTANCE;

  @Override
  public Tracer get(String instrumentationName) {
    return NoopTracer.INSTANCE;
  }

  @Override
  public Tracer get(String instrumentationName, String instrumentationVersion) {
    return NoopTracer.INSTANCE;
  }

  enum NoopTracer implements Tracer {
    INSTANCE;

    @Override
    public SpanBuilder spanBuilder(String spanName) {
      return NoopSpanBuilder.INSTANCE;
    }
  }

  enum NoopSpanBuilder implements SpanBuilder {
    INSTANCE;

    @Override
    public SpanBuilder setParent(Context context) {
      return this;
    }

    @Override
    public SpanBuilder setNoParent() {
      return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext) {
      return this;
    }

    @Override
    public SpanBuilder addLink(SpanContext spanContext, Attributes attributes) {
      return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, String value) {
      return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, long value) {
      return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, double value) {
      return this;
    }

    @Override
    public SpanBuilder setAttribute(String key, boolean value) {
      return this;
    }

    @Override
    public <T> SpanBuilder setAttribute(AttributeKey<T> key, T value) {
      return this;
    }

    @Override
    public SpanBuilder setSpanKind(SpanKind spanKind) {
      return this;
    }

    @Override
    public SpanBuilder setStartTimestamp(long startTimestamp, TimeUnit unit) {
      return this;
    }

    @Override
    public Span startSpan() {
      return Span.getInvalid();
    }
  }
}
