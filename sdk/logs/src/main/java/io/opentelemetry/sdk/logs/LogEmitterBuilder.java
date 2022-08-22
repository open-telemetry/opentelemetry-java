/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;

/** Builder class for creating {@link LogEmitter} instances. */
public interface LogEmitterBuilder {

  /**
   * Assign an OpenTelemetry schema URL to the resulting {@link LogEmitter}.
   *
   * @param schemaUrl the URL of the OpenTelemetry schema being used by this instrumentation scope
   * @return this
   */
  LogEmitterBuilder setSchemaUrl(String schemaUrl);

  /**
   * Assign a version to the instrumentation scope that is using the resulting {@link LogEmitter}.
   *
   * @param instrumentationScopeVersion the version of the instrumentation scope
   * @return this
   */
  LogEmitterBuilder setInstrumentationVersion(String instrumentationScopeVersion);

  /**
   * Assigns instrumentation scope attributes for the resulting {@link LogEmitter}.
   *
   * @param attributes The instrumentation scope attributes.
   * @return this
   */
  default LogEmitterBuilder setAttributes(Attributes attributes) {
    return this;
  }

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @return a log emitter instance configured with the provided options
   */
  LogEmitter build();
}
