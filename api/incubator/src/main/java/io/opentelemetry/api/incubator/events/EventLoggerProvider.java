/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating scoped {@link EventLogger}s. The name <i>Provider</i> is for consistency
 * with other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see EventLogger
 */
@ThreadSafe
public interface EventLoggerProvider {

  /**
   * Gets or creates a named {@link EventLogger} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a Logger instance.
   */
  default EventLogger get(String instrumentationScopeName) {
    return eventLoggerBuilder(instrumentationScopeName).build();
  }

  /**
   * Creates a LoggerBuilder for a named {@link EventLogger} instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @return a LoggerBuilder instance.
   */
  EventLoggerBuilder eventLoggerBuilder(String instrumentationScopeName);

  /**
   * Returns a no-op {@link EventLoggerProvider} which provides Loggers which do not record or emit.
   */
  static EventLoggerProvider noop() {
    return DefaultEventLoggerProvider.getInstance();
  }
}
