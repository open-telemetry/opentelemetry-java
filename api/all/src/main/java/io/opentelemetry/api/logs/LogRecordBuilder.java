/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.ComplexAttribute;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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
   * <p>The {@code timestamp} is the time at which the log record occurred. If unset, it will be set
   * to the current time when {@link #emit()} is called.
   */
  LogRecordBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp}, using the instant.
   *
   * <p>The {@code timestamp} is the time at which the log record occurred. If unset, it will be set
   * to the current time when {@link #emit()} is called.
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
   * Sets attributes to the newly created {@code LogRecord}. If the {@link LogRecordBuilder}
   * previously contained a mapping for any of the keys, the old values are replaced by the
   * specified values.
   *
   * @param attributes the attributes
   * @return this.
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
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value);

  /**
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: the behavior of null values is undefined, and hence strongly discouraged.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default <T> LogRecordBuilder setComplexAttribute(AttributeKey<T> key, ComplexAttribute value) {
    return this;
  }

  /**
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
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
  default LogRecordBuilder setAttribute(String key, String value) {
    return setAttribute(AttributeKey.stringKey(key), value);
  }

  /**
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default LogRecordBuilder setAttribute(String key, long value) {
    return setAttribute(AttributeKey.longKey(key), value);
  }

  /**
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default LogRecordBuilder setAttribute(String key, double value) {
    return setAttribute(AttributeKey.doubleKey(key), value);
  }

  /**
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default LogRecordBuilder setAttribute(String key, boolean value) {
    return setAttribute(AttributeKey.booleanKey(key), value);
  }

  /**
   * Sets an attribute to the newly created {@code LogRecord}. If {@code LogRecordBuilder}
   * previously contained a mapping for the key, the old value is replaced by the specified value.
   *
   * <p>Note: It is strongly recommended to use {@link #setAttribute(AttributeKey, Object)}, and
   * pre-allocate your keys, if possible.
   *
   * @param key the key for this attribute.
   * @param value the value for this attribute.
   * @return this.
   */
  default LogRecordBuilder setAttribute(AttributeKey<Long> key, int value) {
    return setAttribute(key, (long) value);
  }

  /** Emit the log record. */
  void emit();
}
