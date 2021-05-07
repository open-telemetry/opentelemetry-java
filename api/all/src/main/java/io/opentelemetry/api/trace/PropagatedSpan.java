/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import java.util.concurrent.TimeUnit;
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
  //
  // In particular, do not change this return type to PropagatedSpan because auto-instrumentation
  // hijacks this method and returns a bridged implementation of Span.
  //
  // Ideally auto-instrumentation would hijack the public Span.wrap() instead of this
  // method, but auto-instrumentation also needs to inject its own implementation of Span
  // into the class loader at the same time, which causes a problem because injecting a class into
  // the class loader automatically resolves its super classes (interfaces), which in this case is
  // Span, which would be the same class (interface) being instrumented at that time,
  // which would lead to the JVM throwing a LinkageError "attempted duplicate interface definition"
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
    return "PropagatedSpan{" + spanContext + '}';
  }
}
