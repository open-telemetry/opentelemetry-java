/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.internal.ComponentRegistry;
import javax.annotation.Nullable;

/** Builder class for {@link LogEmitter} instances. */
public final class LogEmitterBuilder {

  private final ComponentRegistry<LogEmitter> registry;
  private final String instrumentationName;
  @Nullable private String getInstrumentationVersion;
  @Nullable private String schemaUrl;

  LogEmitterBuilder(ComponentRegistry<LogEmitter> registry, String instrumentationName) {
    this.registry = registry;
    this.instrumentationName = instrumentationName;
  }

  /**
   * Assign an OpenTelemetry schema URL to the resulting {@link LogEmitter}.
   *
   * @param schemaUrl the URL of the OpenTelemetry schema being used by this instrumentation library
   * @return this
   */
  public LogEmitterBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  /**
   * Assign a version to the instrumenation library that is using the resulting {@link LogEmitter}.
   *
   * @param instrumentationVersion the versino of the instrumenation library
   * @return this
   */
  public LogEmitterBuilder setInstrumentationVersion(String instrumentationVersion) {
    this.getInstrumentationVersion = instrumentationVersion;
    return this;
  }

  /**
   * Gets or creates a {@link LogEmitter} instance.
   *
   * @return a log emitter instance configured with the provided options
   */
  public LogEmitter build() {
    return registry.get(instrumentationName, getInstrumentationVersion, schemaUrl);
  }
}
