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

/**
 * Delegates <i>all</i> {@link Span} methods to some underlying Span via {@link
 * ProxyingSpan#getProxied()}.
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
 * a single method, {@link ProxyingSpan#getProxied()}, to simplify and better ensure delegation and
 * meeting expectations.
 */
// todo make this unnecessary
@SuppressWarnings("UngroupedOverloads")
interface ProxyingSpan extends Span {
  Span getProxied();

  // implementations

  @Override
  default <T> Span setAttribute(AttributeKey<T> key, T value) {
    return getProxied().setAttribute(key, value);
  }

  @Override
  default Span addEvent(String name, Attributes attributes) {
    return getProxied().addEvent(name, attributes);
  }

  @Override
  default Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return getProxied().addEvent(name, attributes, timestamp, unit);
  }

  @Override
  default Span setStatus(StatusCode statusCode, String description) {
    return getProxied().setStatus(statusCode, description);
  }

  @Override
  default Span recordException(Throwable exception, Attributes additionalAttributes) {
    return getProxied().recordException(exception, additionalAttributes);
  }

  @Override
  default Span updateName(String name) {
    return getProxied().updateName(name);
  }

  @Override
  default void end() {
    getProxied().end();
  }

  @Override
  default void end(long timestamp, TimeUnit unit) {
    getProxied().end(timestamp, unit);
  }

  @Override
  default SpanContext getSpanContext() {
    return getProxied().getSpanContext();
  }

  @Override
  default boolean isRecording() {
    return getProxied().isRecording();
  }

  // default overrides

  @Override
  default Span setAttribute(String key, String value) {
    return getProxied().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, long value) {
    return getProxied().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, double value) {
    return getProxied().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, boolean value) {
    return getProxied().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(AttributeKey<Long> key, int value) {
    return getProxied().setAttribute(key, value);
  }

  @Override
  default Span setAllAttributes(Attributes attributes) {
    return getProxied().setAllAttributes(attributes);
  }

  @Override
  default Span addEvent(String name) {
    return getProxied().addEvent(name);
  }

  @Override
  default Span addEvent(String name, long timestamp, TimeUnit unit) {
    return getProxied().addEvent(name, timestamp, unit);
  }

  @Override
  default Span addEvent(String name, Instant timestamp) {
    return getProxied().addEvent(name, timestamp);
  }

  @Override
  default Span addEvent(String name, Attributes attributes, Instant timestamp) {
    return getProxied().addEvent(name, attributes, timestamp);
  }

  @Override
  default Span setStatus(StatusCode statusCode) {
    return getProxied().setStatus(statusCode);
  }

  @Override
  default Span recordException(Throwable exception) {
    return getProxied().recordException(exception);
  }

  @Override
  default void end(Instant timestamp) {
    getProxied().end(timestamp);
  }

  @Override
  default Context storeInContext(Context context) {
    return getProxied().storeInContext(context);
  }

  @MustBeClosed
  @Override
  default Scope makeCurrent() {
    return getProxied().makeCurrent();
  }
}
