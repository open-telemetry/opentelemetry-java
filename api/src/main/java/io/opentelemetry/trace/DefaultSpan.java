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
 *
 * <p>Used also to stop tracing, see {@link Tracer#withSpan}.
 *
 * @since 0.1.0
 */
@Immutable
public final class DefaultSpan implements Span {

  private static final DefaultSpan INVALID = new DefaultSpan(SpanContext.getInvalid());

  /**
   * Returns a {@link DefaultSpan} with an invalid {@link SpanContext}.
   *
   * @return a {@code DefaultSpan} with an invalid {@code SpanContext}.
   * @since 0.1.0
   */
  public static Span getInvalid() {
    return INVALID;
  }

  /**
   * Creates an instance of this class with the {@link SpanContext}.
   *
   * @param spanContext the {@code SpanContext}.
   * @return a {@link DefaultSpan}.
   * @since 0.1.0
   */
  public static Span create(SpanContext spanContext) {
    return new DefaultSpan(spanContext);
  }

  private final SpanContext spanContext;

  DefaultSpan(SpanContext spanContext) {
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
  public void setStatus(StatusCanonicalCode canonicalCode) {}

  @Override
  public void setStatus(StatusCanonicalCode canonicalCode, String description) {}

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
