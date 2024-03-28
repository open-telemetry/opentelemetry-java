/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.logs.AnyValue;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** The EventBuilder is used to {@link #emit()} events. */
public interface EventBuilder {

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, String value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, long value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, double value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, boolean value) {
    return put(key, AnyValue.of(value));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, String... value) {
    List<AnyValue<?>> values = new ArrayList<>(value.length);
    for (String val : value) {
      values.add(AnyValue.of(val));
    }
    return put(key, AnyValue.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, long... value) {
    List<AnyValue<?>> values = new ArrayList<>(value.length);
    for (long val : value) {
      values.add(AnyValue.of(val));
    }
    return put(key, AnyValue.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, double... value) {
    List<AnyValue<?>> values = new ArrayList<>(value.length);
    for (double val : value) {
      values.add(AnyValue.of(val));
    }
    return put(key, AnyValue.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  default EventBuilder put(String key, boolean... value) {
    List<AnyValue<?>> values = new ArrayList<>(value.length);
    for (boolean val : value) {
      values.add(AnyValue.of(val));
    }
    return put(key, AnyValue.of(values));
  }

  /** Put the given {@code key} and {@code value} in the payload. */
  EventBuilder put(String key, AnyValue<?> value);

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
   * <p>Event {@link io.opentelemetry.api.common.Attributes} provide additional details about the
   * Event which are not part of the well-defined {@link AnyValue} {@code payload}.
   */
  EventBuilder setAttributes(Attributes attributes);

  /** Emit an event. */
  void emit();
}
