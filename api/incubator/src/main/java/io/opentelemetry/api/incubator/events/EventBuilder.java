/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import java.time.Instant;
import java.util.concurrent.TimeUnit;

/** The EventBuilder is used to {@link #emit()} events. */
public interface EventBuilder {

  /**
   * Set the epoch {@code timestamp} for the event, using the timestamp and unit.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(long timestamp, TimeUnit unit);

  /**
   * Set the epoch {@code timestamp} for the event, using the instant.
   *
   * <p>The {@code timestamp} is the time at which the event occurred. If unset, it will be set to
   * the current time when {@link #emit()} is called.
   */
  EventBuilder setTimestamp(Instant instant);

  /** Emit an event. */
  void emit();
}
