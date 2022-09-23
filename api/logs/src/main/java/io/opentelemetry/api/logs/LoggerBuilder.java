/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.logs;

import io.opentelemetry.api.common.Attributes;

/**
 * Builder class for creating {@link Logger} instances.
 *
 * <p>{@link Logger}s are identified by their scope name, version, and schema URL. These identifying
 * fields, along with attributes, combine to for the instrumentation scope, which is attached to all
 * log records produced by the {@link Logger}.
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
   * Sets the scope instrumentation version of the resulting {@link Logger}. Version is part of
   * {@link Logger} identity.
   *
   * @param instrumentationScopeVersion The instrumentation version.
   * @return this
   */
  LoggerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Sets the scope attributes of the resulting {@link Logger}. Attributes are not part of {@link
   * Logger} identity.
   *
   * <p>Obtaining multiple {@link Logger}s which have the same name, version, and schema URL, but
   * different attributes is not advised and the behavior is unspecified.
   *
   * @param attributes The scope attributes.
   * @return this
   */
  LoggerBuilder setAttributes(Attributes attributes);

  /**
   * Gets or creates a {@link Logger} instance.
   *
   * @return a {@link Logger} instance configured with the provided options.
   */
  Logger build();
}
