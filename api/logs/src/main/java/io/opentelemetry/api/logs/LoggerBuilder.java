/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

/** Builder class for creating {@link Logger} instances. */
public interface LoggerBuilder {

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
