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
   * @param schemaUrl The URL of the OpenTelemetry schema being used by this instrumentation
   *     library.
   * @return this
   */
  TracerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assign a version to the instrumentation library that is using the resulting Tracer.
   *
   * @param instrumentationVersion The version of the instrumentation library.
   * @return this
   */
  TracerBuilder setInstrumentationVersion(String instrumentationVersion);

  /**
   * Gets or creates a {@link Tracer} instance.
   *
   * @return a {@link Tracer} instance configured with the provided options.
   */
  Tracer build();
}
