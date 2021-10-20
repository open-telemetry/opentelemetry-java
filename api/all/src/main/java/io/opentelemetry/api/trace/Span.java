/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.ImplicitContextKeyed;
import java.time.Instant;
import java.util.concurrent.TimeUnit;
import javax.annotation.Nullable;
import javax.annotation.concurrent.ThreadSafe;

/**
 * An interface that represents a span. It has an associated {@link SpanContext}.
 *
 * <p>Spans are created by the {@link SpanBuilder#startSpan} method.
 *
 * <p>{@code Span} <b>must</b> be ended by calling {@link #end()}.
 */
@ThreadSafe
public interface Span extends ImplicitContextKeyed {

  /**
   * Returns the {@link Span} from the current {@link Context}, falling back to a default, no-op
   * {@link Span} if there is no span in the current context.
   */
  static Span current() {
    Span span = Context.current().get(SpanContextKey.KEY);
    return span == null ? getInvalid() : span;
  }

  /**
   * Returns the {@link Span} from the specified {@link Context}, falling back to a default, no-op
   * {@link Span} if there is no span in the context.
   */
  static Span fromContext(Context context) {
    if (context == null) {
      return Span.getInvalid();
    }
    Span span = context.get(SpanContextKey.KEY);
    return span == null ? getInvalid() : span;
  }

  /**
   * Returns the {@link Span} from the specified {@link Context}, or {@code null} if there is no
   * span in the context.
   */
  @Nullable
  static Span fromContextOrNull(Context context) {
    if (context == null) {
      return null;
    }
    return context.get(SpanContextKey.KEY);
  }

  /**
   * Returns an invalid {@link Span}. An invalid {@link Span} is used when tracing is disabled,
   * usually because there is no OpenTelemetry SDK installed.
   */
  static Span getInvalid() {
    return PropagatedSpan.INVALID;
  }

  /**
   * Returns a non-recording {@link Span} that holds the provided {@link SpanContext} but has no
   * functionality. It will not be exported and all tracing operations are no-op, but it can be used
   * to propagate a valid {@link SpanContext} downstream.
   */
  static Span wrap(SpanContext spanContext) {
    if (spanContext == null || !spanContext.isValid()) {
      return getInvalid();
    }
    return PropagatedSpan.create(spanContext);
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>If a null or empty String {@code value} is passed in, the behavior is undefined, and hence
   * strongly discouraged.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default Span setAttribute(String key, @Nullable String value) {
    return setAttribute(AttributeKey.stringKey(key), value);
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default Span setAttribute(String key, long value) {
    return setAttribute(AttributeKey.longKey(key), value);
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default Span setAttribute(String key, double value) {
    return setAttribute(AttributeKey.doubleKey(key), value);
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default Span setAttribute(String key, boolean value) {
    return setAttribute(AttributeKey.booleanKey(key), value);
  }

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  <T> Span setAttribute(AttributeKey<T> key, @Nullable T value);

  /**
   * Sets an attribute to the {@code Span}. If the {@code Span} previously contained a mapping for
   * the key, the old value is replaced by the specified value.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default Span setAttribute(AttributeKey<Long> key, int value) {
    return setAttribute(key, (long) value);
  }

  /**
   * Sets attributes to the {@link Span}. If the {@link Span} previously contained a mapping for any
   * of the keys, the old values are replaced by the specified values.
   *
   * @param attributes the attributes
   * @return this.
   * @since 1.2.0
   */
  @SuppressWarnings("unchecked")
  default Span setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> this.setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /**
   * Adds an event to the {@link Span}. The timestamp of the event will be the current time.
   *
   * @param name the name of the event.
   * @return this.
   */
  default Span addEvent(String name) {
    return addEvent(name, Attributes.empty());
  }

  /**
   * Adds an event to the {@link Span} with the given {@code timestamp}, as nanos since epoch. Note,
   * this {@code timestamp} is not the same as {@link System#nanoTime()} but may be computed using
   * it, for example, by taking a difference of readings from {@link System#nanoTime()} and adding
   * to the span start time.
   *
   * <p>When possible, it is preferred to use {@link #addEvent(String)} at the time the event
   * occurred.
   *
   * @param name the name of the event.
   * @param timestamp the explicit event timestamp since epoch.
   * @param unit the unit of the timestamp
   * @return this.
   */
  default Span addEvent(String name, long timestamp, TimeUnit unit) {
    return addEvent(name, Attributes.empty(), timestamp, unit);
  }

  /**
   * Adds an event to the {@link Span} with the given {@code timestamp}, as nanos since epoch. Note,
   * this {@code timestamp} is not the same as {@link System#nanoTime()} but may be computed using
   * it, for example, by taking a difference of readings from {@link System#nanoTime()} and adding
   * to the span start time.
   *
   * <p>When possible, it is preferred to use {@link #addEvent(String)} at the time the event
   * occurred.
   *
   * @param name the name of the event.
   * @param timestamp the explicit event timestamp since epoch.
   * @return this.
   */
  default Span addEvent(String name, Instant timestamp) {
    if (timestamp == null) {
      return addEvent(name);
    }
    return addEvent(
        name, SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano(), NANOSECONDS);
  }

  /**
   * Adds an event to the {@link Span} with the given {@link Attributes}. The timestamp of the event
   * will be the current time.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@code setAttribute()}.
   * @return this.
   */
  Span addEvent(String name, Attributes attributes);

  /**
   * Adds an event to the {@link Span} with the given {@link Attributes} and {@code timestamp}.
   * Note, this {@code timestamp} is not the same as {@link System#nanoTime()} but may be computed
   * using it, for example, by taking a difference of readings from {@link System#nanoTime()} and
   * adding to the span start time.
   *
   * <p>When possible, it is preferred to use {@link #addEvent(String)} at the time the event
   * occurred.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@code setAttribute()}.
   * @param timestamp the explicit event timestamp since epoch.
   * @param unit the unit of the timestamp
   * @return this.
   */
  Span addEvent(String name, Attributes attributes, long timestamp, TimeUnit unit);

  /**
   * Adds an event to the {@link Span} with the given {@link Attributes} and {@code timestamp}.
   * Note, this {@code timestamp} is not the same as {@link System#nanoTime()} but may be computed
   * using it, for example, by taking a difference of readings from {@link System#nanoTime()} and
   * adding to the span start time.
   *
   * <p>When possible, it is preferred to use {@link #addEvent(String)} at the time the event
   * occurred.
   *
   * @param name the name of the event.
   * @param attributes the attributes that will be added; these are associated with this event, not
   *     the {@code Span} as for {@code setAttribute()}.
   * @param timestamp the explicit event timestamp since epoch.
   * @return this.
   */
  default Span addEvent(String name, Attributes attributes, Instant timestamp) {
    if (timestamp == null) {
      return addEvent(name, attributes);
    }
    return addEvent(
        name,
        attributes,
        SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano(),
        NANOSECONDS);
  }

  /**
   * Sets the status to the {@code Span}.
   *
   * <p>If used, this will override the default {@code Span} status. Default status code is {@link
   * StatusCode#UNSET}.
   *
   * <p>Only the value of the last call will be recorded, and implementations are free to ignore
   * previous calls.
   *
   * @param statusCode the {@link StatusCode} to set.
   * @return this.
   */
  default Span setStatus(StatusCode statusCode) {
    return setStatus(statusCode, "");
  }

  /**
   * Sets the status to the {@code Span}.
   *
   * <p>If used, this will override the default {@code Span} status. Default status code is {@link
   * StatusCode#UNSET}.
   *
   * <p>Only the value of the last call will be recorded, and implementations are free to ignore
   * previous calls.
   *
   * @param statusCode the {@link StatusCode} to set.
   * @param description the description of the {@code Status}.
   * @return this.
   */
  Span setStatus(StatusCode statusCode, String description);

  /**
   * Records information about the {@link Throwable} to the {@link Span}.
   *
   * <p>Note that the EXCEPTION_ESCAPED value from the Semantic Conventions cannot be determined by
   * this function. You should record this attribute manually using {@link
   * #recordException(Throwable, Attributes)} if you know that an exception is escaping.
   *
   * @param exception the {@link Throwable} to record.
   * @return this.
   */
  default Span recordException(Throwable exception) {
    return recordException(exception, Attributes.empty());
  }

  /**
   * Records information about the {@link Throwable} to the {@link Span}.
   *
   * @param exception the {@link Throwable} to record.
   * @param additionalAttributes the additional {@link Attributes} to record.
   * @return this.
   */
  Span recordException(Throwable exception, Attributes additionalAttributes);

  /**
   * Updates the {@code Span} name.
   *
   * <p>If used, this will override the name provided via {@code Span.Builder}.
   *
   * <p>Upon this update, any sampling behavior based on {@code Span} name will depend on the
   * implementation.
   *
   * @param name the {@code Span} name.
   * @return this.
   */
  Span updateName(String name);

  /**
   * Marks the end of {@code Span} execution.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   */
  void end();

  /**
   * Marks the end of {@code Span} execution with the specified timestamp.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * <p>Use this method for specifying explicit end options, such as end {@code Timestamp}. When no
   * explicit values are required, use {@link #end()}.
   *
   * @param timestamp the explicit timestamp from the epoch, for this {@code Span}. {@code 0}
   *     indicates current time should be used.
   * @param unit the unit of the timestamp
   */
  void end(long timestamp, TimeUnit unit);

  /**
   * Marks the end of {@code Span} execution with the specified timestamp.
   *
   * <p>Only the timing of the first end call for a given {@code Span} will be recorded, and
   * implementations are free to ignore all further calls.
   *
   * <p>Use this method for specifying explicit end options, such as end {@code Timestamp}. When no
   * explicit values are required, use {@link #end()}.
   *
   * @param timestamp the explicit timestamp from the epoch, for this {@code Span}. {@code 0}
   *     indicates current time should be used.
   */
  default void end(Instant timestamp) {
    if (timestamp == null) {
      end();
      return;
    }
    end(SECONDS.toNanos(timestamp.getEpochSecond()) + timestamp.getNano(), NANOSECONDS);
  }

  /**
   * Returns the {@code SpanContext} associated with this {@code Span}.
   *
   * @return the {@code SpanContext} associated with this {@code Span}.
   */
  SpanContext getSpanContext();

  /**
   * Returns {@code true} if this {@code Span} records tracing events (e.g. {@link
   * #addEvent(String)}, {@link #setAttribute(String, long)}).
   *
   * @return {@code true} if this {@code Span} records tracing events.
   */
  boolean isRecording();

  @Override
  default Context storeInContext(Context context) {
    return context.with(SpanContextKey.KEY, this);
  }
}
