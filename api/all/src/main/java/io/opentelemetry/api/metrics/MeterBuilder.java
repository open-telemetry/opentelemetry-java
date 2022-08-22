/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

import io.opentelemetry.api.common.Attributes;

/**
 * Builder class for creating {@link Meter} instances.
 *
 * @since 1.10.0
 */
public interface MeterBuilder {

  /**
   * Assigns an OpenTelemetry schema URL to the resulting Meter.
   *
   * @param schemaUrl The URL of the OpenTelemetry schema being used by this instrumentation scope.
   * @return this
   */
  MeterBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assigns a version to the instrumentation scope that is using the resulting Meter.
   *
   * @param instrumentationScopeVersion The version of the instrumentation scope.
   * @return this
   */
  MeterBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Assigns instrumentation scope attributes for the resulting Meter.
   *
   * @param attributes The instrumentation scope attributes.
   * @return this
   * @since 1.18.0
   */
  default MeterBuilder setAttributes(Attributes attributes) {
    return this;
  }

  /**
   * Gets or creates a {@link Meter} instance.
   *
   * @return a {@link Meter} instance configured with the provided options.
   */
  Meter build();
}
