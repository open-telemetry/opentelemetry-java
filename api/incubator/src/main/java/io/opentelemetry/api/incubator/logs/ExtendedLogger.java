/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.logs;

import io.opentelemetry.api.logs.Logger;
import io.opentelemetry.api.logs.Severity;
import io.opentelemetry.context.Context;

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

  @Override
  ExtendedLogRecordBuilder logRecordBuilder();
}
