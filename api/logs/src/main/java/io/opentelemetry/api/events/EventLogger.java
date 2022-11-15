/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.logs.LogRecordBuilder;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import io.opentelemetry.semconv.trace.attributes.SemanticAttributes;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

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
 *     eventLogger
 *       .eventBuilder("my-event")
 *       .setAllAttributes(Attributes.builder()
 *          .put("key1", "value1")
 *          .put("key2", "value2").build())
 *       .emit();
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
   * <p>{@link #eventBuilder(String)} delegates to {@link Logger#logRecordBuilder()}. The {@code
   * eventDomain} is included on every emitted {@code LogRecord} in the {@link
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
   * Return an {@link EventBuilder} to emit an Event.
   *
   * <p>Build the event using the {@link EventBuilder} setters, and emit via {@link
   * EventBuilder#emit()}. The {@code eventName} is included on the emitted {@code LogRecord} in the
   * {@link SemanticAttributes#EVENT_NAME} attribute.
   *
   * @param eventName the event name, which acts as a classifier for events. Within a particular
   *     event domain, event name defines a particular class or type of event.
   */
  public EventBuilder eventBuilder(String eventName) {
    return new EventBuilderImpl(logger.logRecordBuilder(), eventName);
  }

  private class EventBuilderImpl implements EventBuilder {

    private final LogRecordBuilder logRecordBuilder;
    private final String eventName;

    private EventBuilderImpl(LogRecordBuilder logRecordBuilder, String eventName) {
      this.logRecordBuilder = logRecordBuilder;
      this.eventName = eventName;
    }

    @Override
    public LogRecordBuilder setEpoch(long timestamp, TimeUnit unit) {
      logRecordBuilder.setEpoch(timestamp, unit);
      return this;
    }

    @Override
    public LogRecordBuilder setEpoch(Instant instant) {
      logRecordBuilder.setEpoch(instant);
      return this;
    }

    @Override
    public LogRecordBuilder setContext(Context context) {
      logRecordBuilder.setContext(context);
      return this;
    }

    @Override
    public LogRecordBuilder setSeverity(Severity severity) {
      logRecordBuilder.setSeverity(severity);
      return this;
    }

    @Override
    public LogRecordBuilder setSeverityText(String severityText) {
      logRecordBuilder.setSeverityText(severityText);
      return this;
    }

    @Override
    public LogRecordBuilder setBody(String body) {
      logRecordBuilder.setBody(body);
      return this;
    }

    @Override
    public <T> LogRecordBuilder setAttribute(AttributeKey<T> key, T value) {
      logRecordBuilder.setAttribute(key, value);
      return this;
    }

    @Override
    public void emit() {
      logRecordBuilder
          .setAttribute(SemanticAttributes.EVENT_DOMAIN, eventDomain)
          .setAttribute(SemanticAttributes.EVENT_NAME, eventName)
          .emit();
    }
  }
}
