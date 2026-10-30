/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig.internal;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.List;
import java.util.function.Supplier;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class DeclarativeConfigProviderLoader {

  private static final ComponentLoader COMPONENT_LOADER =
      ComponentLoader.forClassLoader(DeclarativeConfigProperties.class.getClassLoader());

  private DeclarativeConfigProviderLoader() {}

  /**
   * Load the highest-ordered implementation of {@code spiClass}, or create its default.
   *
   * @param spiClass the SPI type
   * @param defaultProvider supplies the default provider when none are registered
   * @param <T> the SPI type
   * @return the selected provider
   */
  public static <T extends Ordered> T load(Class<T> spiClass, Supplier<T> defaultProvider) {
    return load(spiClass, defaultProvider, COMPONENT_LOADER);
  }

  static <T extends Ordered> T load(
      Class<T> spiClass, Supplier<T> defaultProvider, ComponentLoader componentLoader) {
    List<T> providers = Ordered.loadOrderedList(componentLoader, spiClass);
    return providers.isEmpty() ? defaultProvider.get() : providers.get(providers.size() - 1);
  }
}
