/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.Value;
import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;
import javax.annotation.Nullable;

/** Extended {@link Logger} with experimental APIs. */
public interface ExtendedLogger extends Logger {

  /**
   * Returns {@code true} if the logger is enabled for the given {@code context} and {@code
   * severity}.
   *
   * <p>This allows callers to avoid unnecessary compute when nothing is consuming the data. Because
   * the response is subject to change over the application, callers should call this before each
   * call to {@link #logRecordBuilder()}.
   */
  default boolean isEnabled(Severity severity, Context context) {
    return true;
  }

  /** Overload of {@link #isEnabled(Severity, Context)} assuming {@link Context#current()}. */
  default boolean isEnabled(Severity severity) {
    return isEnabled(severity, Context.current());
  }

  /**
   * Overload of {@link #isEnabled(Severity, Context)} assuming {@link
   * Severity#UNDEFINED_SEVERITY_NUMBER} and {@link Context#current()}.
   *
   * @deprecated for removal after 1.55.0. Use {@link #isEnabled(Severity, Context)} or {@link
   *     #isEnabled(Severity)} instead.
   */
  @Deprecated
  default boolean isEnabled() {
    return isEnabled(Severity.UNDEFINED_SEVERITY_NUMBER);
  }

  // Fluent log API

  /**
   * A fluent log event API, with convenience methods for incrementally adding fields and
   * attributes.
   *
   * <p>Callers must call {@link LogEventBuilder#emit()}.
   */
  LogEventBuilder logBuilder(Severity severity, String eventName);

  // Low allocation log APIs

  /**
   * A low-allocation alternative to {@link #logBuilder(Severity, String)} which prevents the need
   * for allocating a builder instance.
   *
   * @param severity the log severity number
   * @param eventName the name that identifies the class / type of event which uniquely identifies
   *     the event structure (attributes and body)
   * @param attributes the log attributes, or {@link Attributes#empty()}
   * @param body the log body, or {@code null}
   * @param exception the exception, or {@code null}
   * @param context the context, or {@link Context#current()}
   */
  void log(
      Severity severity,
      String eventName,
      Attributes attributes,
      @Nullable Value<?> body,
      @Nullable Throwable exception,
      Context context);

  default void log(
      Severity severity,
      String eventName,
      Attributes attributes,
      Value<?> body,
      Throwable exception) {
    log(severity, eventName, attributes, body, exception, Context.current());
  }

  default void log(Severity severity, String eventName, Attributes attributes, Value<?> body) {
    log(severity, eventName, attributes, body, null, Context.current());
  }

  default void log(
      Severity severity,
      String eventName,
      Attributes attributes,
      String body,
      Throwable exception) {
    log(severity, eventName, attributes, Value.of(body), exception, Context.current());
  }

  default void log(Severity severity, String eventName, Attributes attributes, String body) {
    log(severity, eventName, attributes, Value.of(body));
  }

  default void log(
      Severity severity, String eventName, Attributes attributes, Throwable exception) {
    log(severity, eventName, attributes, null, exception, Context.current());
  }

  default void log(Severity severity, String eventName, Attributes attributes) {
    log(severity, eventName, attributes, null, null, Context.current());
  }

  default void log(Severity severity, String eventName, Throwable exception) {
    log(severity, eventName, Attributes.empty(), null, exception, Context.current());
  }

  default void log(Severity severity, String eventName) {
    log(severity, eventName, Attributes.empty(), null, null, Context.current());
  }

  // info overloads of log(..)

  /**
   * Overload of {@link #log(Severity, String, Attributes, Value, Throwable, Context)} assuming
   * {@link Severity#INFO}.
   */
  default void info(
      String eventName,
      Attributes attributes,
      @Nullable Value<?> body,
      @Nullable Throwable exception,
      Context context) {
    log(Severity.INFO, eventName, attributes, body, exception, context);
  }

  default void info(String eventName, Attributes attributes, Value<?> body, Throwable exception) {
    info(eventName, attributes, body, exception, Context.current());
  }

  default void info(String eventName, Attributes attributes, Value<?> body) {
    info(eventName, attributes, body, null, Context.current());
  }

  default void info(String eventName, Attributes attributes, String body, Throwable exception) {
    info(eventName, attributes, Value.of(body), exception, Context.current());
  }

  default void info(String eventName, Attributes attributes, String body) {
    info(eventName, attributes, Value.of(body));
  }

  default void info(String eventName, Attributes attributes, Throwable exception) {
    info(eventName, attributes, null, exception, Context.current());
  }

  default void info(String eventName, Attributes attributes) {
    info(eventName, attributes, null, null, Context.current());
  }

  default void info(String eventName, Throwable exception) {
    info(eventName, Attributes.empty(), null, exception, Context.current());
  }

  default void info(String eventName) {
    info(eventName, Attributes.empty(), null, null, Context.current());
  }

  // TODO: add severity overloads for trace, debug, warn, error, fatal

  @Override
  ExtendedLogRecordBuilder logRecordBuilder();
}
