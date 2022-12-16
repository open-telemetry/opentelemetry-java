/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.metrics;

/**
 * Builder class for creating {@link Meter} instances.
 *
 * <p>{@link Meter}s are identified by their scope name, version, and schema URL. These identifying
 * fields, along with attributes, combine to form the instrumentation scope, which is attached to
 * all metrics produced by the {@link Meter}.
 *
 * @since 1.10.0
 */
public interface MeterBuilder {

  /**
   * Set the scope schema URL of the resulting {@link Meter}. Schema URL is part of {@link Meter}
   * identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  MeterBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link Meter}. Version is part of
   * {@link Meter} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
   * @return this
   */
  MeterBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Gets or creates a {@link Meter} instance.
   *
   * @return a {@link Meter} instance configured with the provided options.
   */
  Meter build();
}
