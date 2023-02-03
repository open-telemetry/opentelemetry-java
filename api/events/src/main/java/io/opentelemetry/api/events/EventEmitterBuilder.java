/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

/**
 * Builder class for creating {@link EventEmitter} instances.
 *
 * <p>{@link EventEmitter}s are identified by their scope name, version, and schema URL. These
 * identifying fields, along with attributes, combine to form the instrumentation scope, which is
 * attached to all events produced by the {@link EventEmitter}.
 */
public interface EventEmitterBuilder {

  /**
   * Sets the event domain. Event domain is not part of {@link EventEmitter} identity.
   *
   * @param eventDomain The event domain, which acts as a namespace for event names. Within a
   *     particular event domain, event name defines a particular class or type of event.
   * @return this
   */
  EventEmitterBuilder setEventDomain(String eventDomain);

  /**
   * Set the scope schema URL of the resulting {@link EventEmitter}. Schema URL is part of {@link
   * EventEmitter} identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  EventEmitterBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link EventEmitter}. Version is part
   * of {@link EventEmitter} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  EventEmitterBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link EventEmitter} instance.
   *
   * @return a {@link EventEmitter} instance configured with the provided options.
   */
  EventEmitter build();
}
