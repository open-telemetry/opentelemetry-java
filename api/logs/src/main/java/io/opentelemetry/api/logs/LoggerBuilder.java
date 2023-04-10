/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.context.Context;

/**
 * Builder class for creating {@link Logger} instances.
 *
 * <p>{@link Logger}s are identified by their scope name, version, and schema URL. These identifying
 * fields, along with attributes, combine to form the instrumentation scope, which is attached to
 * all log records produced by the {@link Logger}.
 */
public interface LoggerBuilder {

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
   * Specify that emitted log records SHOULD NOT automatically include trace context from the {@link
   * Context#current()}.
   *
   * @return this
   */
  LoggerBuilder excludeTraceContext();

  /**
   * Gets or creates a {@link Logger} instance.
   *
   * @return a {@link Logger} instance configured with the provided options.
   */
  Logger build();
}
