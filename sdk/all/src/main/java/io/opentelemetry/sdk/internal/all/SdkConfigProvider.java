/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.internal.all;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;

/**
 * SDK implementation of {@link ConfigProvider}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class SdkConfigProvider implements ConfigProvider {

  private final DeclarativeConfigProperties instrumentationConfig;

  private SdkConfigProvider(DeclarativeConfigProperties openTelemetryConfigModel) {
    this.instrumentationConfig = openTelemetryConfigModel.get("instrumentation/development");
  }

  /**
   * Create a {@link SdkConfigProvider}.
   *
   * @param openTelemetryConfigModel {@link DeclarativeConfigProperties} corresponding to the {@code
   *     OpenTelemetryConfiguration} type, i.e. the root node.
   * @return the {@link SdkConfigProvider} instance
   */
  public static SdkConfigProvider create(DeclarativeConfigProperties openTelemetryConfigModel) {
    return new SdkConfigProvider(openTelemetryConfigModel);
  }

  @Override
  public DeclarativeConfigProperties getInstrumentationConfig() {
    return instrumentationConfig;
  }

  @Override
  public String toString() {
    return "SdkConfigProvider{" + "instrumentationConfig=" + instrumentationConfig + '}';
  }
}
