/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.trace;

/**
 * Builder class for creating {@link Tracer} instances.
 *
 * <p>{@link Tracer}s are identified by their scope name, version, and schema URL. These identifying
 * fields, along with attributes, combine to form the instrumentation scope, which is attached to
 * all spans produced by the {@link Tracer}.
 *
 * @since 1.4.0
 */
public interface TracerBuilder {

  /**
   * Set the scope schema URL of the resulting {@link Tracer}. Schema URL is part of {@link Tracer}
   * identity.
   *
   * @param schemaUrl The schema URL.
   * @return this
   */
  TracerBuilder setSchemaUrl(String schemaUrl);

  /**
   * Sets the instrumentation scope version of the resulting {@link Tracer}. Version is part of
   * {@link Tracer} identity.
   *
   * @param instrumentationScopeVersion The instrumentation scope version.
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
