/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.extension.incubator.logs.AnyValue;
import java.util.Map;
import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link EventEmitter} is the entry point into an event pipeline.
 *
 * <p>Example usage emitting events:
 *
 * <p>// TODO: rework
 *
 * <pre>{@code
 * class MyClass {
 *   private final EventEmitter eventEmitter = openTelemetryEventEmitterProvider.eventEmitterBuilder("scope-name")
 *         .build();
 *
 *   void doWork() {
 *     eventEmitter.emit("namespace.my-event", AnyValue.of(Map.of(
 *        "key1", AnyValue.of("value1"),
 *        "key2", AnyValue.of("value2"))));
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
   * @param eventName the event name, which defines the class or type of event. Events names SHOULD
   *     include a namespace to avoid collisions with other event names.
   * @param payload the eventPayload, which is expected to match the schema of other events with the
   *     same {@code eventName}.
   */
  void emit(String eventName, AnyValue<?> payload);

  /**
   * Emit an event.
   *
   * @param eventName the event name, which defines the class or type of event. Events names SHOULD
   *     include a namespace to avoid collisions with other event names.
   * @param payload the eventPayload, which is expected to match the schema of other events with the
   *     same {@code eventName}.
   */
  default void emit(String eventName, Map<String, AnyValue<?>> payload) {
    emit(eventName, AnyValue.of(payload));
  }

  /**
   * Return a {@link EventBuilder} to emit an event.
   *
   * @param eventName the event name, which defines the class or type of event. Events names SHOULD
   *     include a namespace to avoid collisions with other event names.
   */
  EventBuilder builder(String eventName);
}
