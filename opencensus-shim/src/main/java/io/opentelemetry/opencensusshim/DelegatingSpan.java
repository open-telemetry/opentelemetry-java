/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.opencensusshim;

import com.google.errorprone.annotations.MustBeClosed;
import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.StatusCode;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;

/**
 * Delegates <i>all</i> {@link Span} methods to some underlying Span via {@link
 * DelegatingSpan#getDelegate()}.
 *
 * <p>If not all calls are proxied, some, such as and in particular {@link
 * Span#storeInContext(Context)} and {@link Span#makeCurrent()}, will use {@code this} instead of
 * the proxied {@link Span} which betrays the expectation of instance fidelity imposed by the
 * underlying otel mechanisms which minted the original {@link Span}, such as the otel javaagent.
 *
 * <p>This proxy class simplification allows the shim to perform its duties as minimally invasively
 * as possible and itself never expose its own classes and objects to callers or recipients of calls
 * from the shim.
 *
 * <p>This addresses the inconsistency where not all methods are appropriately delegated by exposing
 * a single method, {@link DelegatingSpan#getDelegate()}, to simplify and better ensure delegation
 * and meeting expectations.
 */
interface DelegatingSpan extends Span {
  Span getDelegate();

  @Override
  default Span updateName(String name) {
    return getDelegate().updateName(name);
  }

  @Override
  default SpanContext getSpanContext() {
    return getDelegate().getSpanContext();
  }

  @Override
  default boolean isRecording() {
    return getDelegate().isRecording();
  }

  @Override
  default <T> Span setAttribute(AttributeKey<T> key, @Nullable T value) {
    return getDelegate().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, @Nullable String value) {
    return getDelegate().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, long value) {
    return getDelegate().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, double value) {
    return getDelegate().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, boolean value) {
    return getDelegate().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(AttributeKey<Long> key, int value) {
    return getDelegate().setAttribute(key, value);
  }

  @Override
  default Span setAllAttributes(Attributes attributes) {
    return getDelegate().setAllAttributes(attributes);
  }

  @Override
  default Span addEvent(String name, Attributes attributes) {
    return getDelegate().addEvent(name, attributes);
  }

  @Override
  default Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return getDelegate().addEvent(name, attributes, timestamp, unit);
  }

  @Override
  default Span addEvent(String name) {
    return getDelegate().addEvent(name);
  }

  @Override
  default Span addEvent(String name, long timestamp, TimeUnit unit) {
    return getDelegate().addEvent(name, timestamp, unit);
  }

  @Override
  default Span addEvent(String name, Instant timestamp) {
    return getDelegate().addEvent(name, timestamp);
  }

  @Override
  default Span addEvent(String name, Attributes attributes, Instant timestamp) {
    return getDelegate().addEvent(name, attributes, timestamp);
  }

  @Override
  default Span setStatus(StatusCode statusCode, String description) {
    return getDelegate().setStatus(statusCode, description);
  }

  @Override
  default Span setStatus(StatusCode statusCode) {
    return getDelegate().setStatus(statusCode);
  }

  @Override
  default Span recordException(Throwable exception, Attributes additionalAttributes) {
    return getDelegate().recordException(exception, additionalAttributes);
  }

  @Override
  default Span recordException(Throwable exception) {
    return getDelegate().recordException(exception);
  }

  @Override
  default void end(Instant timestamp) {
    getDelegate().end(timestamp);
  }

  @Override
  default void end() {
    getDelegate().end();
  }

  @Override
  default void end(long timestamp, TimeUnit unit) {
    getDelegate().end(timestamp, unit);
  }

  @Override
  default Context storeInContext(Context context) {
    return getDelegate().storeInContext(context);
  }

  @MustBeClosed
  @Override
  default Scope makeCurrent() {
    return getDelegate().makeCurrent();
  }
}
