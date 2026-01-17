/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import static io.opentelemetry.api.incubator.config.DeclarativeConfigProperties.empty;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.extension.incubator.fileconfig.internal.model.OpenTelemetryConfigurationModel;

/** SDK implementation of {@link ConfigProvider}. */
public final class SdkConfigProvider implements ConfigProvider {

  private final DeclarativeConfigProperties instrumentationConfig;

  private SdkConfigProvider(
      OpenTelemetryConfigurationModel model, ComponentLoader componentLoader) {
    this(
        DeclarativeConfiguration.toConfigProperties(model, componentLoader)
            .getStructured("instrumentation/development", empty()));
  }

  private SdkConfigProvider(DeclarativeConfigProperties instrumentationConfig) {
    this.instrumentationConfig = instrumentationConfig;
  }

  /**
   * Create a {@link SdkConfigProvider} from the {@code model}.
   *
   * @param model the configuration model
   * @return the {@link SdkConfigProvider}
   */
  public static SdkConfigProvider create(OpenTelemetryConfigurationModel model) {
    return create(model, ComponentLoader.forClassLoader(SdkConfigProvider.class.getClassLoader()));
  }

  /**
   * Create a {@link SdkConfigProvider} from the {@code model}.
   *
   * @param model the configuration model
   * @param componentLoader the component loader used to load SPIs
   * @return the {@link SdkConfigProvider}
   */
  public static SdkConfigProvider create(
      OpenTelemetryConfigurationModel model, ComponentLoader componentLoader) {
    return new SdkConfigProvider(model, componentLoader);
  }

  /**
   * Create a no-op {@link SdkConfigProvider}.
   *
   * @return the no-op {@link SdkConfigProvider}
   */
  public static SdkConfigProvider noop() {
    return new SdkConfigProvider(DeclarativeConfigProperties.empty());
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
