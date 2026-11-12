/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.common.impl.ConfigUtil;
import java.util.List;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DeclarativeConfigProviderLoader {

  private static final Logger LOGGER =
      Logger.getLogger(DeclarativeConfigProviderLoader.class.getName());

  private static final ComponentLoader COMPONENT_LOADER =
      ComponentLoader.forClassLoader(DeclarativeConfigProperties.class.getClassLoader());

  private DeclarativeConfigProviderLoader() {}

  /**
   * Load the first discovered implementation of {@code spiClass}, or the configured implementation
   * if multiple are registered. Create the default provider when none are registered.
   *
   * @param spiClass the SPI type
   * @param defaultProvider supplies the default provider when none are registered
   * @param configProperty the system property or environment variable used to select a provider
   * @param <T> the SPI type
   * @return the selected provider
   */
  public static <T> T loadFirstOrConfigured(
      Class<T> spiClass, Supplier<T> defaultProvider, String configProperty) {
    return loadFirstOrConfigured(
        spiClass,
        defaultProvider,
        configProperty,
        COMPONENT_LOADER,
        ConfigUtil.getString(configProperty, ""));
  }

  static <T> T loadFirstOrConfigured(
      Class<T> spiClass,
      Supplier<T> defaultProvider,
      String configProperty,
      ComponentLoader componentLoader,
      String configuredProvider) {
    List<T> providers = ComponentLoader.loadList(componentLoader, spiClass);
    if (providers.isEmpty()) {
      return defaultProvider.get();
    }
    if (providers.size() == 1) {
      return providers.get(0);
    }

    if (configuredProvider.isEmpty()) {
      if (LOGGER.isLoggable(Level.WARNING)) {
        LOGGER.log(
            Level.WARNING,
            "Multiple {0} providers found. Please include only one, or specify preference "
                + "setting {1} to the FQCN of the preferred provider.",
            new Object[] {spiClass.getSimpleName(), configProperty});
      }
      return providers.get(0);
    }

    for (T provider : providers) {
      if (provider.getClass().getName().equals(configuredProvider)) {
        return provider;
      }
    }
    throw new IllegalStateException(
        "No "
            + spiClass.getSimpleName()
            + " matched configured "
            + configProperty
            + ": "
            + configuredProvider);
  }
}
