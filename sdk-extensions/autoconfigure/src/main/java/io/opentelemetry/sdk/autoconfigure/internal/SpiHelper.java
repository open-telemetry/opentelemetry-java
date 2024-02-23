/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableProvider;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class SpiHelper {

  private final ComponentLoader componentLoader;
  private final Set<AutoConfigureListener> listeners =
      Collections.newSetFromMap(new IdentityHashMap<>());

  private SpiHelper(ComponentLoader componentLoader) {
    this.componentLoader = componentLoader;
  }

  /** Create a {@link SpiHelper} which loads SPIs using the {@code classLoader}. */
  public static SpiHelper create(ClassLoader classLoader) {
    return new SpiHelper(new ServiceLoaderComponentLoader(classLoader));
  }

  public static SpiHelper create(ComponentLoader componentLoader) {
    return new SpiHelper(componentLoader);
  }

  /**
   * Load implementations of an SPI which are configurable (i.e. they accept {@link
   * ConfigProperties}.
   *
   * @param <T> the configurable type
   * @param <S> the SPI type
   * @param spiClass the SPI class
   * @param getConfigurable function returning a configured instance
   * @param config the configuration to pass to invocations of {@code #getConfigurable}
   * @return a {@link NamedSpiManager} used to access configured instances of the SPI by name
   */
  public <T, S extends ConfigurableProvider> NamedSpiManager<T> loadConfigurable(
      Class<S> spiClass,
      BiFunction<S, ConfigProperties, T> getConfigurable,
      ConfigProperties config) {
    Map<String, Supplier<T>> nameToProvider = new HashMap<>();
    Map<String, S> providers = componentLoader.loadConfigurableProviders(spiClass);
    for (Map.Entry<String, S> entry : providers.entrySet()) {
      S provider = entry.getValue();
      String name = entry.getKey();
      // both the provider and the result may have a listener
      maybeAddListener(provider);

      nameToProvider.put(name, () -> maybeAddListener(getConfigurable.apply(provider, config)));
    }
    return NamedSpiManager.create(nameToProvider);
  }

  /**
   * Load implementations of an ordered SPI (i.e. implements {@link Ordered}).
   *
   * @param spiClass the SPI class
   * @param <T> the SPI type
   * @return list of SPI implementations, in order
   */
  public <T extends Ordered> List<T> loadOrdered(Class<T> spiClass) {
    return init(componentLoader.loadOrdered(spiClass));
  }

  /**
   * Load implementations of an SPI.
   *
   * @param spiClass the SPI class
   * @param <T> the SPI type
   * @return list of SPI implementations
   */
  public <T> List<T> load(Class<T> spiClass) {
    return init(componentLoader.load(spiClass));
  }

  /**
   * Load implementations of an SPI.
   *
   * @param components the SPI implementations
   * @param <T> the SPI type
   * @return list of SPI implementations
   */
  private <T> List<T> init(Iterable<T> components) {
    List<T> result = new ArrayList<>();
    for (T service : components) {
      result.add(maybeAddListener(service));
    }
    return result;
  }

  private <T> T maybeAddListener(T object) {
    if (object instanceof AutoConfigureListener) {
      listeners.add((AutoConfigureListener) object);
    }
    return object;
  }

  /** Return the set of SPIs loaded which implement {@link AutoConfigureListener}. */
  public Set<AutoConfigureListener> getListeners() {
    return Collections.unmodifiableSet(listeners);
  }

  private static class ServiceLoaderComponentLoader implements ComponentLoader {
    private final ClassLoader classLoader;

    private ServiceLoaderComponentLoader(ClassLoader classLoader) {
      this.classLoader = classLoader;
    }

    @Override
    public <T> Iterable<T> load(Class<T> spiClass) {
      return ServiceLoader.load(spiClass, classLoader);
    }
  }
}
