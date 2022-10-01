/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Logger} is the entry point into a log pipeline.
 *
 * <p>Obtain a {@link EventBuilder} or {@link #logRecordBuilder()}, add properties using the
 * setters, and emit it via {@link LogRecordBuilder#emit()}.
 *
 * <p>Example usage emitting events:
 *
 * <pre>{@code
 * class MyClass {
 *   private final Logger eventLogger = openTelemetryLoggerProvider.loggerBuilder("instrumentation-library-name")
 *     .setInstrumentationVersion("1.0.0")
 *     .setEventDomain("acme.observability")
 *     .build();
 *
 *   void doWork() {
 *     eventLogger.eventBuilder("my-event")
 *       .setAllAttributes(Attributes.builder()
 *         .put("key1", "value1")
 *         .put("key2", "value2")
 *         .build())
 *       .emit();
 *     // do work
 *   }
 * }
 * }</pre>
 */
@ThreadSafe
public interface Logger {

  /**
   * Return a {@link EventBuilder} to emit an event.
   *
   * <p><b>NOTE:</b> this API MUST only be called on {@link Logger}s which have been assigned an
   * {@link LoggerBuilder#setEventDomain(String) event domain}.
   *
   * <p>Build the event using the {@link EventBuilder} setters, and emit via {@link
   * EventBuilder#emit()}.
   *
   * @param eventName the event name, which acts as a classifier for events. Within a particular
   *     event domain, event name defines a particular class or type of event.
   */
  EventBuilder eventBuilder(String eventName);

  /**
   * Return a {@link LogRecordBuilder} to emit a log record.
   *
   * <p><b>IMPORTANT:</b> this should be used to write appenders to bridge logs from logging
   * frameworks (e.g. SLF4J, Log4j, JUL, Logback, etc). It is <b>NOT</b> a replacement for an
   * application logging framework, and should not be used by application developers.
   *
   * <p>Build the log record using the {@link LogRecordBuilder} setters, and emit via {@link
   * LogRecordBuilder#emit()}.
   */
  LogRecordBuilder logRecordBuilder();
}
