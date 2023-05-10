package io.opentelemetry.api.trace;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.Scope;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

public interface CloseableSpan extends Span, AutoCloseable {

  static CloseableSpan noop(Span span) {
    return new CloseableSpan() {
      @Override
      public Span getSpan() {
        return span;
      }

      @Override
      public Scope getScope() {
        return Scope.noop();
      }
    };
  }

  Span getSpan();

  Scope getScope();

  @Override
  default void close() {
    getScope().close();
    getSpan().end();
  }

  @Override
  default <T> Span setAttribute(AttributeKey<T> key, T value) {
    return getSpan().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, String value) {
    return getSpan().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, long value) {
    return getSpan().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(String key, double value) {
    return getSpan().setAttribute(key, value);

  }

  @Override
  default Span setAttribute(String key, boolean value) {
    return getSpan().setAttribute(key, value);
  }

  @Override
  default Span setAttribute(AttributeKey<Long> key, int value) {
    return getSpan().setAttribute(key, value);
  }

  @Override
  default Span setAllAttributes(Attributes attributes) {
    return getSpan().setAllAttributes(attributes);
  }

  @Override
  default Span addEvent(String name, Attributes attributes) {
    return getSpan().addEvent(name, attributes);
  }

  @Override
  default Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit) {
    return getSpan().addEvent(name, attributes, timestamp, unit);
  }

  @Override
  default Span addEvent(String name) {
    return getSpan().addEvent(name);
  }

  @Override
  default Span addEvent(String name, long timestamp, TimeUnit unit) {
    return getSpan().addEvent(name, timestamp, unit);
  }

  @Override
  default Span addEvent(String name, Instant timestamp) {
    return getSpan().addEvent(name, timestamp);
  }

  @Override
  default Span addEvent(String name, Attributes attributes, Instant timestamp) {
    return getSpan().addEvent(name, attributes, timestamp);
  }

  @Override
  default Span setStatus(StatusCode statusCode, String description) {
    return getSpan().setStatus(statusCode, description);
  }

  @Override
  default Span setStatus(StatusCode statusCode) {
    return getSpan().setStatus(statusCode);
  }

  @Override
  default Span recordException(Throwable exception) {
    return getSpan().recordException(exception);
  }

  @Override
  default Span recordException(Throwable exception, Attributes additionalAttributes) {
    return getSpan().recordException(exception, additionalAttributes);
  }

  @Override
  default Span updateName(String name) {
    return getSpan().updateName(name);
  }

  @Override
  default void end() {
    getSpan().end();
  }

  @Override
  default void end(Instant timestamp) {
    getSpan().end(timestamp);
  }

  @Override
  default Context storeInContext(Context context) {
    return getSpan().storeInContext(context);
  }

  @Override
  default void end(long timestamp, TimeUnit unit) {
    getSpan().end(timestamp, unit);
  }

  @Override
  default SpanContext getSpanContext() {
    return getSpan().getSpanContext();
  }

  @Override
  default boolean isRecording() {
    return getSpan().isRecording();
  }
}
