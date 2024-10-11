/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import static java.util.stream.Collectors.toList;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The EventBuilder is used to {@link #emit()} events. */
public interface EventBuilder {

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, String value) {
    return put(key, Value.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, long value) {
    return put(key, Value.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, double value) {
    return put(key, Value.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, boolean value) {
    return put(key, Value.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, String... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (String val : value) {
      values.add(Value.of(val));
    }
    return put(key, Value.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, long... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (long val : value) {
      values.add(Value.of(val));
    }
    return put(key, Value.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, double... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (double val : value) {
      values.add(Value.of(val));
    }
    return put(key, Value.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, boolean... value) {
    List<Value<?>> values = new ArrayList<>(value.length);
    for (boolean val : value) {
      values.add(Value.of(val));
    }
    return put(key, Value.of(values));
  }

  /**
   * Put the given key and value in the payload.
   *
   * <p>NOTE: The key value pair is NOT added to the event attributes. Setting event attributes is
   * less common than adding entries to the event payload. Use {@link #setAttributes(Attributes)} if
   * intending the data to be set in attributes instead of the payload.
   */
  @SuppressWarnings("unchecked")
  default <T> EventBuilder put(AttributeKey<T> key, T value) {
    switch (key.getType()) {
      case STRING:
        return put(key.getKey(), (String) value);
      case BOOLEAN:
        return put(key.getKey(), (boolean) value);
      case LONG:
        return put(key.getKey(), (long) value);
      case DOUBLE:
        return put(key.getKey(), (double) value);
      case STRING_ARRAY:
        return put(
            key.getKey(),
            Value.of(((List<String>) value).stream().map(Value::of).collect(toList())));
      case BOOLEAN_ARRAY:
        return put(
            key.getKey(),
            Value.of(((List<Boolean>) value).stream().map(Value::of).collect(toList())));
      case LONG_ARRAY:
        return put(
            key.getKey(), Value.of(((List<Long>) value).stream().map(Value::of).collect(toList())));
      case DOUBLE_ARRAY:
        return put(
            key.getKey(),
            Value.of(((List<Double>) value).stream().map(Value::of).collect(toList())));
    }
    return this;
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  EventBuilder put(String key, Value<?> value);

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp}, using the instant.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(Instant instant);

  /** Set the context. */
  EventBuilder setContext(Context context);

  /** Set the severity. */
  EventBuilder setSeverity(Severity severity);

  /**
   * Set the attributes.
   *
   * <p>Event {@link Attributes} provide additional details about the Event which are not part of
   * the well-defined {@link Value} payload. Setting event attributes is less common than adding
   * entries to the event payload. Most users will want to call one of the {@code #put(String, ?)}
   * methods instead.
   */
  EventBuilder setAttributes(Attributes attributes);

  /** Emit an event. */
  void emit();
}
