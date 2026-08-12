/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.internal;

import static java.util.Objects.requireNonNull;

import io.opentelemetry.sdk.autoconfigure.declarativeconfig.model.LoggerProviderModel;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nullable;

/**
 * Provides typed access to experimental properties on {@link LoggerProviderModel}.
 *
 * <p>This class is internal and experimental. Its APIs are unstable and can change at any time. Its
 * APIs (or a version of them) may be promoted to the public stable API in the future, but no
 * guarantees are made.
 */
public final class LoggerProviderModelAccessor {

  private LoggerProviderModelAccessor() {}

  static final String LOGGER_CONFIGURATOR = "logger_configurator/development";

  public static final Map<String, Class<?>> EXPERIMENTAL_PROPERTIES;

  static {
    EXPERIMENTAL_PROPERTIES = new HashMap<>();
    EXPERIMENTAL_PROPERTIES.put(LOGGER_CONFIGURATOR, ExperimentalLoggerConfiguratorModel.class);
  }

  @Nullable
  public static ExperimentalLoggerConfiguratorModel getLoggerConfigurator(
      LoggerProviderModel model) {
    return ExtensionPropertyUtil.get(
        LOGGER_CONFIGURATOR,
        model.getExtensionProperties(),
        ExperimentalLoggerConfiguratorModel.class);
  }

  public static LoggerProviderModel withLoggerConfigurator(
      LoggerProviderModel model, ExperimentalLoggerConfiguratorModel value) {
    requireNonNull(value, "value");
    model.withExtensionProperty(LOGGER_CONFIGURATOR, value);
    return model;
  }
}
