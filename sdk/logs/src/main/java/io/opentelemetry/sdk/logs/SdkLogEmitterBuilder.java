/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.common.InstrumentationScopeInfo;
import io.opentelemetry.sdk.common.InstrumentationScopeInfoBuilder;
import io.opentelemetry.sdk.internal.ComponentRegistry;

final class SdkLogEmitterBuilder implements LogEmitterBuilder {

  private final ComponentRegistry<SdkLogEmitter> registry;
  private final InstrumentationScopeInfoBuilder scopeBuilder;

  SdkLogEmitterBuilder(ComponentRegistry<SdkLogEmitter> registry, String instrumentationScopeName) {
    this.registry = registry;
    this.scopeBuilder = InstrumentationScopeInfo.builder(instrumentationScopeName);
  }

  @Override
  public SdkLogEmitterBuilder setSchemaUrl(String schemaUrl) {
    scopeBuilder.setSchemaUrl(schemaUrl);
    return this;
  }

  @Override
  public SdkLogEmitterBuilder setInstrumentationVersion(String instrumentationScopeVersion) {
    scopeBuilder.setVersion(instrumentationScopeVersion);
    return this;
  }

  @Override
  public LogEmitterBuilder setAttributes(Attributes attributes) {
    scopeBuilder.setAttributes(attributes);
    return this;
  }

  @Override
  public SdkLogEmitter build() {
    return registry.get(scopeBuilder.build());
  }
}
