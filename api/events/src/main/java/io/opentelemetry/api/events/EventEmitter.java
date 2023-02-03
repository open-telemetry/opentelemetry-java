/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link EventEmitter} is the entry point into an event pipeline.
 *
 * <p>Example usage emitting events:
 *
 * <pre>{@code
 * class MyClass {
 *   private final EventEmitter eventEmitter = openTelemetryEventEmitterProvider.eventEmitterBuilder("scope-name")
 *         .setEventDomain("acme.observability")
 *         .build();
 *
 *   void doWork() {
 *     eventEmitter.emit("my-event", Attributes.builder()
 *         .put("key1", "value1")
 *         .put("key2", "value2")
 *         .build())
 *     // do work
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface EventEmitter {

  /**
   * Emit an event.
   *
   * @param eventName the event name, which acts as a classifier for events. Within a particular
   *     event domain, event name defines a particular class or type of event.
   * @param attributes attributes associated with the event
   */
  void emit(String eventName, Attributes attributes);
}
