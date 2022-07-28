/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

final class SpiUtil {

  interface ServiceLoaderFinder {

    <S> Iterable<S> load(Class<S> spiClass, ClassLoader classLoader);
  }

  static <T, U> NamedSpiManager<T> loadConfigurable(
      Class<U> spiClass,
      Function<U, String> getName,
      BiFunction<U, ConfigProperties, T> getConfigurable,
      ConfigProperties config,
      ClassLoader serviceClassLoader) {
    return loadConfigurable(
        spiClass, getName, getConfigurable, config, serviceClassLoader, ServiceLoader::load);
  }

  // VisibleForTesting
  static <T, U> NamedSpiManager<T> loadConfigurable(
      Class<U> spiClass,
      Function<U, String> getName,
      BiFunction<U, ConfigProperties, T> getConfigurable,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      ServiceLoaderFinder serviceLoaderFinder) {
    Map<String, Supplier<T>> nameToProvider = new HashMap<>();
    for (U provider : serviceLoaderFinder.load(spiClass, serviceClassLoader)) {
      String name = getName.apply(provider);
      nameToProvider.put(name, () -> getConfigurable.apply(provider, config));
    }
    return NamedSpiManager.create(nameToProvider);
  }

  static <T extends Ordered> List<T> loadOrdered(
      Class<T> spiClass, ClassLoader serviceClassLoader) {
    return loadOrdered(spiClass, serviceClassLoader, ServiceLoader::load);
  }

  // VisibleForTesting
  static <T extends Ordered> List<T> loadOrdered(
      Class<T> spiClass, ClassLoader serviceClassLoader, ServiceLoaderFinder serviceLoaderFinder) {
    List<T> result = new ArrayList<>();
    for (T service : serviceLoaderFinder.load(spiClass, serviceClassLoader)) {
      result.add(service);
    }
    result.sort(Comparator.comparing(Ordered::order));
    return result;
  }

  private SpiUtil() {}
}
