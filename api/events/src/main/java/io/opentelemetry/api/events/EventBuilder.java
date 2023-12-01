/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.extension.incubator.logs.AnyValue;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/** The EventBuilder is used to {@link #emit()} events. */
public interface EventBuilder {

  /**
   * Set the {@code payload}.
   *
   * <p>The {@code payload} is expected to match the schema of other events with the same {@code
   * eventName}.
   */
  EventBuilder setPayload(AnyValue<?> payload);

  /**
   * Set the {@code payload}.
   *
   * <p>The {@code payload} is expected to match the schema of other events with the same {@code
   * eventName}.
   */
  default EventBuilder setPayload(Map<String, AnyValue<?>> payload) {
    return setPayload(AnyValue.of(payload));
  }

  /**
   * Set the epoch {@code timestamp}, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp}t, using the instant.
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
