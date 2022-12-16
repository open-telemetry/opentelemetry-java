/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.events;

/** Builder class for creating {@link EventEmitter} instances. */
public interface EventEmitterBuilder {

  /**
   * Assign an OpenTelemetry schema URL to the resulting {@link EventEmitter}.
   *
   * @param schemaUrl the URL of the OpenTelemetry schema being used by this instrumentation scope
   * @return this
   */
  EventEmitterBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assign a version to the instrumentation scope that is using the resulting {@link EventEmitter}.
   *
   * @param instrumentationScopeVersion the version of the instrumentation scope
   * @return this
   */
  EventEmitterBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link EventEmitter} instance.
   *
   * @return a logger instance configured with the provided options
   */
  EventEmitter build();
}
