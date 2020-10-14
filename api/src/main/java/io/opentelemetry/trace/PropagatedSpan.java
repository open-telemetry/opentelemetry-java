/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.trace;

import io.opentelemetry.common.AttributeKey;
import io.opentelemetry.common.Attributes;
import javax.annotation.concurrent.Immutable;

/**
 * The {@code DefaultSpan} is the default {@link Span} that is used when no {@code Span}
 * implementation is available. All operations are no-op except context propagation.
 */
@Immutable
final class PropagatedSpan implements Span {

  static final PropagatedSpan INVALID = new PropagatedSpan(SpanContext.getInvalid());

  private final SpanContext spanContext;

  PropagatedSpan(SpanContext spanContext) {
    this.spanContext = spanContext;
  }

  @Override
  public void setAttribute(String key, String value) {}

  @Override
  public void setAttribute(String key, long value) {}

  @Override
  public void setAttribute(String key, double value) {}

  @Override
  public void setAttribute(String key, boolean value) {}

  @Override
  public <T> void setAttribute(AttributeKey<T> key, T value) {}

  @Override
  public void addEvent(String name) {}

  @Override
  public void addEvent(String name, long timestamp) {}

  @Override
  public void addEvent(String name, Attributes attributes) {}

  @Override
  public void addEvent(String name, Attributes attributes, long timestamp) {}

  @Override
  public void setStatus(StatusCode canonicalCode) {}

  @Override
  public void setStatus(StatusCode canonicalCode, String description) {}

  @Override
  public void recordException(Throwable exception) {}

  @Override
  public void recordException(Throwable exception, Attributes additionalAttributes) {}

  @Override
  public void updateName(String name) {}

  @Override
  public void end() {}

  @Override
  public void end(EndSpanOptions endOptions) {}

  @Override
  public SpanContext getContext() {
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
