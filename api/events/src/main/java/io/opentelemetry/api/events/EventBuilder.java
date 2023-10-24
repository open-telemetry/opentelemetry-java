/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.Attributes;

/** The EventBuilder is used to emit() events. */
public interface EventBuilder {

  /** Set the name of the event. */
  EventBuilder setEventName(String eventName);

  /** Set the attributes to attach to the event. */
  EventBuilder setAttributes(Attributes attributes);

  /** Sets the timestamp for the event. */
  EventBuilder setTimestamp(long epochNanos);

  /** Emit an event. */
  void emit();
}
