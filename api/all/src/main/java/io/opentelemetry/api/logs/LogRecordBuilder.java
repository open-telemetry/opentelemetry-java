/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import io.opentelemetry.api.common.AttributeKey;
import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.context.Context;

/**
 * Used to construct and emit log records from a {@link Logger}.
 *
 * <p>Obtain a {@link Logger#logRecordBuilder()}, add properties using the setters, and emit the log
 * record by calling {@link #emit()}.
 *
 * @since 1.27.0
 */
public interface LogRecordBuilder {

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>Note: If not set, the emitted log will not have a timestamp.
   */
  LogRecordBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp}, using the instant.
   *
   * <p>Note: If not set, the emitted log will not have a timestamp.
   */
  LogRecordBuilder setTimestamp(Instant instant);

  /**
   * Set the epoch {@code observedTimestamp}, using the timestamp and unit.
   *
   * <p>The {@code observedTimestamp} is the time at which the log record was observed. If unset, it
   * will be set to the {@code timestamp}. {@code observedTimestamp} may be different from {@code
   * timestamp} if logs are being processed asynchronously (e.g. from a file or on a different
   * thread).
   */
  LogRecordBuilder setObservedTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the {@code observedTimestamp}, using the instant.
   *
   * <p>The {@code observedTimestamp} is the time at which the log record was observed. If unset, it
   * will be set to the {@code timestamp}. {@code observedTimestamp} may be different from {@code
   * timestamp} if logs are being processed asynchronously (e.g. from a file or on a different
   * thread).
   */
  LogRecordBuilder setObservedTimestamp(Instant instant);

  /** Set the context. */
  LogRecordBuilder setContext(Context context);

  /** Set the severity. */
  LogRecordBuilder setSeverity(Severity severity);

  /** Set the severity text. */
  LogRecordBuilder setSeverityText(String severityText);

  /**
   * Set the body string.
   *
   * <p>Shorthand for calling {@link #setBody(Value)} with {@link Value#of(String)}.
   */
  LogRecordBuilder setBody(String body);

  /**
   * Set the body {@link Value}.
   *
   * @since 1.42.0
   */
  default LogRecordBuilder setBody(Value<?> body) {
    setBody(body.asString());
    return this;
  }

  /**
   * Sets attributes. If the {@link LogRecordBuilder} previously contained a mapping for any of the
   * keys, the old values are replaced by the specified values.
   */
  @SuppressWarnings("unchecked")
  default LogRecordBuilder setAllAttributes(Attributes attributes) {
    if (attributes == null || attributes.isEmpty()) {
      return this;
    }
    attributes.forEach(
        (attributeKey, value) -> setAttribute((AttributeKey<Object>) attributeKey, value));
    return this;
  }

  /**
   * Sets an attribute on the {@code LogRecord}. If the {@code LogRecord} previously contained a
   * mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: Providing a null value is a no-op and will not remove previously set values.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  <T> LogRecordBuilder setAttribute(AttributeKey<T> key, @Nullable T value);

  /**
   * Sets a String attribute on the {@code LogRecord}. If the {@code LogRecord} previously contained
   * a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: Providing a null value is a no-op and will not remove previously set values.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   * @since 1.48.0
   */
  default LogRecordBuilder setAttribute(String key, @Nullable String value) {
    return setAttribute(stringKey(key), value);
  }

  /**
   * Sets a Long attribute on the {@code LogRecord}. If the {@code LogRecord} previously contained a
   * mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   * @since 1.48.0
   */
  default LogRecordBuilder setAttribute(String key, long value) {
    return setAttribute(longKey(key), value);
  }

  /**
   * Sets a Double attribute on the {@code LogRecord}. If the {@code LogRecord} previously contained
   * a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   * @since 1.48.0
   */
  default LogRecordBuilder setAttribute(String key, double value) {
    return setAttribute(doubleKey(key), value);
  }

  /**
   * Sets a Boolean attribute on the {@code LogRecord}. If the {@code LogRecord} previously
   * contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   * @since 1.48.0
   */
  default LogRecordBuilder setAttribute(String key, boolean value) {
    return setAttribute(booleanKey(key), value);
  }

  /**
   * Sets an Integer attribute on the {@code LogRecord}. If the {@code LogRecord} previously
   * contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   * @since 1.48.0
   */
  default LogRecordBuilder setAttribute(String key, int value) {
    return setAttribute(key, (long) value);
  }

  /**
   * Sets the event name, which identifies the class / type of the Event.
   *
   * <p>This name should uniquely identify the event structure (both attributes and body). A log
   * record with a non-empty event name is an Event.
   *
   * @since 1.50.0
   */
  default LogRecordBuilder setEventName(String eventName) {
    return this;
  }

  /** Emit the log record. */
  void emit();
}
