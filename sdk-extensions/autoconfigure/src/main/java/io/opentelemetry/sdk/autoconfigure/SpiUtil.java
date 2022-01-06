/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;

final class SpiUtil {

  private static final Logger logger = Logger.getLogger(SpiUtil.class.getName());

  static <T, U> Map<String, T> loadConfigurable(
      Class<U> spiClass,
      Collection<String> requestedNames,
      Function<U, String> getName,
      BiFunction<U, ConfigProperties, T> getConfigurable,
      ConfigProperties config,
      ClassLoader serviceClassLoader) {
    Map<String, T> result = new HashMap<>();
    for (U provider : ServiceLoader.load(spiClass, serviceClassLoader)) {
      String name = getName.apply(provider);
      T configurable;
      try {
        configurable = getConfigurable.apply(provider, config);
      } catch (Throwable t) {
        Level level = requestedNames.contains(name) ? Level.WARNING : Level.FINE;
        logger.log(
            level, "Error initializing " + spiClass.getSimpleName() + " with name " + name, t);
        continue;
      }
      result.put(name, configurable);
    }
    return result;
  }

  private SpiUtil() {}
}
