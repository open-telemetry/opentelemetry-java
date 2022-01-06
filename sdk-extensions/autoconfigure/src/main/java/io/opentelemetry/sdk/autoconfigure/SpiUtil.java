/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Nullable;

final class SpiUtil {

  private static final Logger logger = Logger.getLogger(SpiUtil.class.getName());

  interface ServiceLoaderFinder {

    <S> Iterable<S> load(Class<S> spiClass, ClassLoader classLoader);
  }

  static <T, U> NamedSpiManager<T> loadConfigurable(
      Class<U> spiClass,
      Collection<String> requestedNames,
      Function<U, String> getName,
      BiFunction<U, ConfigProperties, T> getConfigurable,
      ConfigProperties config,
      ClassLoader serviceClassLoader) {
    return loadConfigurable(
        spiClass,
        requestedNames,
        getName,
        getConfigurable,
        config,
        serviceClassLoader,
        ServiceLoader::load);
  }

  // VisibleForTesting
  static <T, U> NamedSpiManager<T> loadConfigurable(
      Class<U> spiClass,
      Collection<String> requestedNames,
      Function<U, String> getName,
      BiFunction<U, ConfigProperties, T> getConfigurable,
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      ServiceLoaderFinder serviceLoaderFinder) {
    Map<String, Supplier<T>> nameToProvider = new HashMap<>();
    for (U provider : serviceLoaderFinder.load(spiClass, serviceClassLoader)) {
      String name = getName.apply(provider);
      nameToProvider.put(
          name,
          () -> {
            try {
              return getConfigurable.apply(provider, config);
            } catch (Throwable t) {
              Level level = requestedNames.contains(name) ? Level.WARNING : Level.FINE;
              logger.log(
                  level,
                  "Error initializing " + spiClass.getSimpleName() + " with name " + name,
                  t);
              return null;
            }
          });
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
