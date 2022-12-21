/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

/**
 * Builder class for creating {@link Logger} instances.
 *
 * <p>{@link Logger}s are identified by their scope name, version, and schema URL. These identifying
 * fields, along with attributes, combine to form the instrumentation scope, which is attached to
 * all log records produced by the {@link Logger}.
 */
public interface LoggerBuilder {

  /**
   * Set the event domain of the resulting {@link Logger}.
   *
   * <p><b>NOTE:</b> Event domain is required to use {@link Logger#eventBuilder(String)}.
   *
   * <p>The event domain will be included in the {@code event.domain} attribute of every event
   * produced by the resulting {@link Logger}.
   *
   * @param eventDomain The event domain, which acts as a namespace for event names. Within a
   *     particular event domain, event name defines a particular class or type of event.
   * @return this
   */
  LoggerBuilder setEventDomain(String eventDomain);

  /**
   * Set the scope schema URL of the resulting {@link Logger}. Schema URL is part of {@link Logger}
   * identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  LoggerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link Logger}. Version is part of
   * {@link Logger} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link Logger} instance.
   *
   * @return a {@link Logger} instance configured with the provided options.
   */
  Logger build();
}
