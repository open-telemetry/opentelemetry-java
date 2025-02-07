/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringArrayKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.List;
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

  /** Sets an attribute. */
  <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value);

  /** Sets a string attribute. */
  default LogRecordBuilder setAttribute(String key, String value) {
    return setAttribute(stringKey(key), value);
  }

  /** Sets a Long attribute. */
  default LogRecordBuilder setAttribute(String key, Long value) {
    return setAttribute(longKey(key), value);
  }

  /** Sets a Double attribute. */
  default LogRecordBuilder setAttribute(String key, Double value) {
    return setAttribute(doubleKey(key), value);
  }

  /** Sets a Boolean attribute. */
  default LogRecordBuilder setAttribute(String key, Boolean value) {
    return setAttribute(booleanKey(key), value);
  }

  /** Sets an Integer attribute. */
  default LogRecordBuilder setAttribute(String key, Integer value) {
    return setAttribute(key, value.longValue());
  }

  /** Sets a string array attribute. */
  default LogRecordBuilder setAttribute(String key, List<String> value) {
    return setAttribute(stringArrayKey(key), value);
  }

  /** Emit the log record. */
  void emit();
}
