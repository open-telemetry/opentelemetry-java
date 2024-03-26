/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import io.opentelemetry.api.common.Attributes;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link EventLogger} is the entry point into an event pipeline.
 *
 * <p>Example usage emitting events:
 *
 * <pre>{@code
 * class MyClass {
 *   private final EventLogger eventLogger = eventLoggerProvider
 *         .eventLoggerBuilder("scope-name")
 *         .build();
 *
 *   void doWork() {
 *     eventLogger.emit("my-namespace.my-event", Attributes.builder()
 *         .put("key1", "value1")
 *         .put("key2", "value2")
 *         .build())
 *     // do work
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface EventLogger {

  /**
   * Emit an event.
   *
   * @param eventName the event name, which identifies the class or type of event. Event with the
   *     same name are structurally similar to one another. Event names are subject to the same
   *     naming rules as attribute names. Notably, they are namespaced to avoid collisions. See <a
   *     href="https://opentelemetry.io/docs/specs/semconv/general/events/">event.name semantic
   *     conventions</a> for more details.
   * @param attributes attributes associated with the event
   */
  void emit(String eventName, Attributes attributes);

  /**
   * Return a {@link EventBuilder} to emit an event.
   *
   * @param eventName the event name, which identifies the class or type of event. Event with the
   *     same name are structurally similar to one another. Event names are subject to the same
   *     naming rules as attribute names. Notably, they are namespaced to avoid collisions. See <a
   *     href="https://opentelemetry.io/docs/specs/semconv/general/events/">event.name semantic
   *     conventions</a> for more details.
   * @param attributes attributes associated with the event
   */
  EventBuilder builder(String eventName, Attributes attributes);
}
