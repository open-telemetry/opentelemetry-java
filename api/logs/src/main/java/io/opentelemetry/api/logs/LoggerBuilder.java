/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

/** Builder class for creating {@link Logger} instances. */
public interface LoggerBuilder {

  /**
   * Set the event domain of the resulting {@link Logger}.
   *
   * <p><b>NOTE:</b> Event domain is required to use {@link Logger#eventBuilder(String)}.
   *
   * <p>The event domain will be included in the {@code event.domain} attribute of every event
   * produced by the resulting {@link Logger}.
   *
   * @param eventDomain The event domain.
   * @return this
   */
  LoggerBuilder setEventDomain(String eventDomain);

  /**
   * Assign an OpenTelemetry schema URL to the resulting {@link Logger}.
   *
   * @param schemaUrl the URL of the OpenTelemetry schema being used by this instrumentation scope
   * @return this
   */
  LoggerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assign a version to the instrumentation scope that is using the resulting {@link Logger}.
   *
   * @param instrumentationScopeVersion the version of the instrumentation scope
   * @return this
   */
  LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link Logger} instance.
   *
   * @return a logger instance configured with the provided options
   */
  Logger build();
}
