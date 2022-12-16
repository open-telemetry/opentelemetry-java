/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

import javax.annotation.concurrent.ThreadSafe;

/**
 * A registry for creating scoped {@link EventEmitter}s. The name <i>Provider</i> is for consistency
 * with other languages and it is <b>NOT</b> loaded using reflection.
 *
 * @see EventEmitter
 */
@ThreadSafe
public interface EventEmitterProvider {

  /**
   * Gets or creates a named EventEmitter instance which emits events to the {@code eventDomain}.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @param eventDomain The event domain, which acts as a namespace for event names. Within a
   *     particular event domain, event name defines a particular class or type of event.
   * @return a Logger instance.
   */
  default EventEmitter get(String instrumentationScopeName, String eventDomain) {
    return eventEmitterBuilder(instrumentationScopeName, eventDomain).build();
  }

  /**
   * Creates a LoggerBuilder for a named EventEmitter instance.
   *
   * @param instrumentationScopeName A name uniquely identifying the instrumentation scope, such as
   *     the instrumentation library, package, or fully qualified class name. Must not be null.
   * @param eventDomain The event domain, which acts as a namespace for event names. Within a
   *     particular event domain, event name defines a particular class or type of event.
   * @return a LoggerBuilder instance.
   */
  EventEmitterBuilder eventEmitterBuilder(String instrumentationScopeName, String eventDomain);

  /**
   * Returns a no-op {@link EventEmitterProvider} which provides Loggers which do not record or
   * emit.
   */
  static EventEmitterProvider noop() {
    return DefaultEventEmitterProvider.getInstance();
  }
}
