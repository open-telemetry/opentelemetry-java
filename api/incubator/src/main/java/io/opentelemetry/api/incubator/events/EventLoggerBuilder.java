/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.events;

/**
 * Builder class for creating {@link EventLogger} instances.
 *
 * <p>{@link EventLogger}s are identified by their scope name, version, and schema URL. These
 * identifying fields, along with attributes, combine to form the instrumentation scope, which is
 * attached to all events produced by the {@link EventLogger}.
 */
public interface EventLoggerBuilder {

  /**
   * Set the scope schema URL of the resulting {@link EventLogger}. Schema URL is part of {@link
   * EventLogger} identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  EventLoggerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link EventLogger}. Version is part of
   * {@link EventLogger} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  EventLoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link EventLogger} instance.
   *
   * @return a {@link EventLogger} instance configured with the provided options.
   */
  EventLogger build();
}
