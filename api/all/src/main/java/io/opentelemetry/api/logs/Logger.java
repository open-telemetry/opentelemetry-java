/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.context.Context;
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
   * Returns {@code true} if the logger is enabled for the given {@code context} and {@code
   * severity}.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #logRecordBuilder()}.
   *
   * @since 1.61.0
   */
  default boolean isEnabled(Severity severity, Context context) {
    return true;
  }

  /**
   * Overload of {@link #isEnabled(Severity, Context)} assuming {@link Context#current()}.
   *
   * @since 1.61.0
   */
  default boolean isEnabled(Severity severity) {
    return isEnabled(severity, Context.current());
  }

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
