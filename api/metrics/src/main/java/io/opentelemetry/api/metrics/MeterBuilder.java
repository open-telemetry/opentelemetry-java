/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/**
 * Builder class for creating {@link Meter} instances.
 *
 * @since 1.4.0
 */
public interface MeterBuilder {

  /**
   * Assign an OpenTelemetry schema URL to the resulting Meter.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used by this instrumentation
   *     library.
   * @return this
   */
  MeterBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assign a version to the instrumentation library that is using the resulting Meter.
   *
   * @param instrumentationVersion The version of the instrumentation library.
   * @return this
   */
  MeterBuilder setInstrumentationVersion(String instrumentationVersion);

  /**
   * Gets or creates a {@link Meter} instance.
   *
   * @return a {@link Meter} instance configured with the provided options.
   */
  Meter build();
}
