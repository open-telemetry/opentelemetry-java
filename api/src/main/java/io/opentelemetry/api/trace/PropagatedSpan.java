/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.Immutable;

/**
 * The default {@link Span} that is used when no {@code Span} implementation is available. All
 * operations are no-op except context propagation.
 */
@Immutable
final class PropagatedSpan implements Span {

  static final PropagatedSpan INVALID = new PropagatedSpan(SpanContext.getInvalid());

  // Used by auto-instrumentation agent. Check with auto-instrumentation before making changes to
  // this method.
  static Span create(SpanContext spanContext) {
    return new PropagatedSpan(spanContext);
  }

  private final SpanContext spanContext;

  private PropagatedSpan(SpanContext spanContext) {
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
  public Span addEvent(String name) {
    return this;
  }

  @Override
  public Span addEvent(String name, long timestamp) {
    return this;
  }

  @Override
  public Span addEvent(String name, Attributes attributes) {
    return this;
  }

  @Override
  public Span addEvent(String name, Attributes attributes, long timestamp) {
    return this;
  }

  @Override
  public Span setStatus(StatusCode canonicalCode) {
    return this;
  }

  @Override
  public Span setStatus(StatusCode canonicalCode, String description) {
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
  public void end(long timestamp) {}

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
    return "DefaultSpan";
  }
}
