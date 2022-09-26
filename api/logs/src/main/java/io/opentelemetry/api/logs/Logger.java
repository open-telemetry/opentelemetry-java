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
 */
@ThreadSafe
public interface Logger {

  /**
   * Return a {@link EventBuilder} to emit an event.
   *
   * <p><b>NOTE:</b> this API can only be called on {@link Logger}s which have been assigned an
   * {@link LoggerBuilder#setEventDomain(String) event domain}.
   *
   * <p>Build the event using the {@link EventBuilder} setters, and emit via {@link
   * EventBuilder#emit()}.
   *
   * @throws IllegalStateException if called on a {@link Logger} that was not assigned a {@link
   *     LoggerBuilder#setEventDomain(String) event domain}.
   */
  EventBuilder eventBuilder(String name);

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
