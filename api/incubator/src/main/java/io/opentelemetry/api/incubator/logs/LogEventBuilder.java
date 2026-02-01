/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import static io.opentelemetry.api.common.AttributeKey.booleanKey;
import static io.opentelemetry.api.common.AttributeKey.doubleKey;
import static io.opentelemetry.api.common.AttributeKey.longKey;
import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.context.Context;
import javax.annotation.Nullable;

/**
 * Experimental fluent builder for <a
 * href="https://opentelemetry.io/docs/specs/semconv/general/events/">log events</a>.
 */
public interface LogEventBuilder {

  /** Set the context. */
  LogEventBuilder setContext(Context context);

  /** Set the body {@link Value}. */
  LogEventBuilder setBody(Value<?> body);

  /**
   * Set the body string.
   *
   * <p>Shorthand for calling {@link #setBody(Value)} with {@link Value#of(String)}.
   */
  LogEventBuilder setBody(String body);

  /**
   * Sets attributes. If the {@link LogRecordBuilder} previously contained a mapping for any of the
   * keys, the old values are replaced by the specified values.
   */
  @SuppressWarnings("unchecked")
  default LogEventBuilder setAllAttributes(Attributes attributes) {
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
  <T> LogEventBuilder setAttribute(AttributeKey<T> key, @Nullable T value);

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
   */
  default LogEventBuilder setAttribute(String key, @Nullable String value) {
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
   */
  default LogEventBuilder setAttribute(String key, long value) {
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
   */
  default LogEventBuilder setAttribute(String key, double value) {
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
   */
  default LogEventBuilder setAttribute(String key, boolean value) {
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
   */
  default LogEventBuilder setAttribute(String key, int value) {
    return setAttribute(key, (long) value);
  }

  /** Set standard {@code exception.*} attributes based on the {@code throwable}. */
  LogEventBuilder setException(Throwable throwable);

  /** Emit the log event. */
  void emit();
}
