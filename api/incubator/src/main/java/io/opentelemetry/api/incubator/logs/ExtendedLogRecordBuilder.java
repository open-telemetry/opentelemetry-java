/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.LogRecordBuilder;

/** Extended {@link LogRecordBuilder} with experimental APIs. */
public interface ExtendedLogRecordBuilder extends LogRecordBuilder {

  // keep this class even if it is empty, since experimental methods may be added in the future.

  /**
   * Sets the event name, which identifies the class / type of the Event.
   *
   * <p>This name should uniquely identify the event structure (both attributes and body). A log
   * record with a non-empty event name is an Event.
   */
  ExtendedLogRecordBuilder setEventName(String eventName);
}
