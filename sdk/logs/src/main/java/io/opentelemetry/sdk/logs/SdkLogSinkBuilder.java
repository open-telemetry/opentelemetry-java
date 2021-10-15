/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.logs;

import io.opentelemetry.sdk.internal.ComponentRegistry;
import javax.annotation.Nullable;

public final class SdkLogSinkBuilder {

  private final ComponentRegistry<SdkLogSink> registry;
  private final String instrumentationName;
  @Nullable private String getInstrumentationVersion;
  @Nullable private String schemaUrl;

  SdkLogSinkBuilder(ComponentRegistry<SdkLogSink> registry, String instrumentationName) {
    this.registry = registry;
    this.instrumentationName = instrumentationName;
  }

  public SdkLogSinkBuilder setSchemaUrl(String schemaUrl) {
    this.schemaUrl = schemaUrl;
    return this;
  }

  public SdkLogSinkBuilder setInstrumentationVersion(String instrumentationVersion) {
    this.getInstrumentationVersion = instrumentationVersion;
    return this;
  }

  public LogSink build() {
    return registry.get(instrumentationName, getInstrumentationVersion, schemaUrl);
  }
}
