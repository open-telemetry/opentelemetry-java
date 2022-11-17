/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.extension.event;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;

/**
 * {@link EventLogger} provides convenience methods for emitting Events. An Event is a {@code
 * LogRecord} that conform to the <a
 * href="https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/logs/semantic_conventions/events.md">Event
 * Semantic Conventions</a>.
 *
 * <p>Example usage:
 *
 * <pre>{@code
 * class MyClass {
 *   private final LoggerProvider loggerProvider = GlobalLoggerProvider.get();
 *   // Create EventLogger by wrapping Logger and setting event domain to "acme-observability"
 *   private final EventLogger eventLogger =
 *     EventLogger.create(
 *       loggerProvider.get("instrumentation-library-name"), "acme.observability");
 *
 *   void doWork() {
 *     eventLogger.emitEvent("my-event", Attributes.builder()
 *          .put("key1", "value1")
 *          .put("key2", "value2").build())
 *   }
 * }
 * }</pre>
 */
public final class EventLogger {

  private final Logger logger;
  private final String eventDomain;

  private EventLogger(Logger logger, String eventDomain) {
    this.logger = logger;
    this.eventDomain = eventDomain;
  }

  /**
   * Create a new {@link EventLogger}.
   *
   * <p>{@link #emitEvent(String, Attributes)} delegates to {@link Logger#logRecordBuilder()}. The
   * {@code eventDomain} is included on every emitted {@code LogRecord} in the {@link
   * SemanticAttributes#EVENT_DOMAIN} attribute.
   *
   * @param logger the delegate {@link Logger}
   * @param eventDomain the event domain, which acts as a namespace for event names. Within a
   *     particular event domain, event name defines a particular class or type of event.
   * @return an {@link EventLogger} instance
   */
  public static EventLogger create(Logger logger, String eventDomain) {
    return new EventLogger(logger, eventDomain);
  }

  /**
   * Emit an event with the {@code eventName} and {@code attributes}.
   *
   * <p>The {@code eventName} is included on the emitted {@code LogRecord} in the {@link
   * SemanticAttributes#EVENT_NAME} attribute.
   *
   * @param eventName the event name, which acts as a classifier for events. Within a particular
   *     event domain, event name defines a particular class or type of event.
   * @param attributes the event attributes
   */
  public void emitEvent(String eventName, Attributes attributes) {
    logger
        .logRecordBuilder()
        .setAllAttributes(attributes)
        .setAttribute(SemanticAttributes.EVENT_DOMAIN, eventDomain)
        .setAttribute(SemanticAttributes.EVENT_NAME, eventName)
        .emit();
  }
}
