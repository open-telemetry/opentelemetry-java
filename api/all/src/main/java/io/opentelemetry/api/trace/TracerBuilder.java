/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * Builder class for creating {@link Tracer} instances.
 *
 * @since 1.4.0
 */
public interface TracerBuilder {

  /**
   * Assign an OpenTelemetry schema URL to the resulting Tracer.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used by this instrumentation scope.
   * @return this
   */
  TracerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assign a version to the instrumentation scope that is using the resulting Tracer.
   *
   * @param instrumentationScopeVersion The version of the instrumentation scope.
   * @return this
   */
  TracerBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link Tracer} instance.
   *
   * @return a {@link Tracer} instance configured with the provided options.
   */
  Tracer build();
}
