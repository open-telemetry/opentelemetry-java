/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A {@link Logger} is the entry point into a log pipeline.
 *
 * <p>Obtain a {@link #logRecordBuilder()}, add properties using the setters, and emit it via {@link
 * LogRecordBuilder#emit()}.
 *
 * <p>The OpenTelemetry logs bridge API exists to enable bridging logs from other log frameworks
 * (e.g. SLF4J, Log4j, JUL, Logback, etc) into OpenTelemetry and is <b>NOT</b> a replacement log
 * API.
 *
 * @since 1.27.0
 */
@ThreadSafe
public interface Logger {

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

  /**
   * Return a {@link LogRecordBuilder} to emit an event.
   *
   * @param eventName the event name, which identifies the class or type of event. Event with the
   *     same name are structurally similar to one another. Event names are subject to the same
   *     naming rules as attribute names. Notably, they are namespaced to avoid collisions. See <a
   *     href="https://opentelemetry.io/docs/specs/semconv/general/events/">event.name semantic
   *     conventions</a> for more details.
   */
  default EventBuilder eventBuilder(String eventName) {
    return DefaultLogger.getInstance().eventBuilder(eventName);
  }
}
