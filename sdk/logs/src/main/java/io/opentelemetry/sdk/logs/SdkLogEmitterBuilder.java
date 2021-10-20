/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.internal.ComponentRegistry;
import javax.annotation.Nullable;

class SdkLogEmitterBuilder implements LogEmitterBuilder {

  private final ComponentRegistry<SdkLogEmitter> registry;
  private final String instrumentationName;
  @Nullable private String getInstrumentationVersion;
  @Nullable private String schemaUrl;

  SdkLogEmitterBuilder(ComponentRegistry<SdkLogEmitter> registry, String instrumentationName) {
    this.registry = registry;
    this.instrumentationName = instrumentationName;
  }

  @Override
  public SdkLogEmitterBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  @Override
  public SdkLogEmitterBuilder setInstrumentationVersion(String instrumentationVersion) {
    this.getInstrumentationVersion = instrumentationVersion;
    return this;
  }

  @Override
  public SdkLogEmitter build() {
    return registry.get(instrumentationName, getInstrumentationVersion, schemaUrl);
  }
}
