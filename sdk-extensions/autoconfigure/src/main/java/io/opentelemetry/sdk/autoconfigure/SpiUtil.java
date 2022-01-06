/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import javax.annotation.Nullable;

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
    return new LazyLoadingNamedSpiManager<>(nameToProvider);
  }

  private SpiUtil() {}

  private static class LazyLoadingNamedSpiManager<T> implements NamedSpiManager<T> {

    private final Map<String, Supplier<T>> nameToProvider;
    private final ConcurrentMap<String, Optional<T>> nameToImplementation =
        new ConcurrentHashMap<>();

    LazyLoadingNamedSpiManager(Map<String, Supplier<T>> nameToProvider) {
      this.nameToProvider = nameToProvider;
    }

    @Override
    @Nullable
    public T getByName(String name) {
      return nameToImplementation
          .computeIfAbsent(name, this::tryLoadImplementationForName)
          .orElse(null);
    }

    private Optional<T> tryLoadImplementationForName(String name) {
      Supplier<T> provider = nameToProvider.get(name);
      if (provider == null) {
        return Optional.empty();
      }

      return Optional.ofNullable(provider.get());
    }
  }
}
