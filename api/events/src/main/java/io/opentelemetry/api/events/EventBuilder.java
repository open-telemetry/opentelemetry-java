/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

/** The EventBuilder is used to emit() events. */
public interface EventBuilder {

  /** Sets the timestamp for the event. */
  EventBuilder setTimestamp(long epochNanos);

  /** Emit an event. */
  void emit();
}
