/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opentracingshim;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import java.util.concurrent.TimeUnit;
import javax.annotation.concurrent.Immutable;

/**
 * The {@link Span} that is used for trace context propagation that allow invalid {@link
 * SpanContext} with sampled TraceFlags to support controlling sampling decision from the inbound
 * request without a parent span using OpenTracingShim, e.g. jaeger-debug-id in jaeger All
 * operations are no-op except context propagation.
 *
 * <p>The {@link Span}.wrap() cannot be used because it doesn't allow invalid {@link SpanContext}.
 */
@Immutable
class PropagatedOpenTelemetrySpan implements Span {
  static Span create(SpanContext spanContext) {
    return new PropagatedOpenTelemetrySpan(spanContext);
  }

  private final SpanContext spanContext;

  private PropagatedOpenTelemetrySpan(SpanContext spanContext) {
    this.spanContext = spanContext;
  }

  @Override
  public Span setAttribute(String key, String value) {
    return this;
  }

  @Override
  public Span setAttribute(String key, long value) {
    return this;
  }

  @Override
  public Span setAttribute(String key, double value) {
    return this;
  }

  @Override
  public Span setAttribute(String key, boolean value) {
    return this;
  }

  @Override
  public <T> Span setAttribute(AttributeKey<T> key, T value) {
    return this;
  }

  @Override
  public Span setAllAttributes(Attributes attributes) {
    return this;
  }

  @Override
  public Span addEvent(String name) {
    return this;
  }

  @Override
  public Span addEvent(String name, long timestamp, TimeUnit unit) {
    return this;
  }

  @Override
  public Span addEvent(String name, Attributes attributes) {
    return this;
  }

  @Override
  public Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return this;
  }

  @Override
  public Span setStatus(StatusCode statusCode) {
    return this;
  }

  @Override
  public Span setStatus(StatusCode statusCode, String description) {
    return this;
  }

  @Override
  public Span recordException(Throwable exception) {
    return this;
  }

  @Override
  public Span recordException(Throwable exception, Attributes additionalAttributes) {
    return this;
  }

  @Override
  public Span updateName(String name) {
    return this;
  }

  @Override
  public void end() {}

  @Override
  public void end(long timestamp, TimeUnit unit) {}

  @Override
  public SpanContext getSpanContext() {
    return spanContext;
  }

  @Override
  public boolean isRecording() {
    return false;
  }

  @Override
  public String toString() {
    return "PropagatedOpenTelemetrySpan{" + spanContext + '}';
  }
}
