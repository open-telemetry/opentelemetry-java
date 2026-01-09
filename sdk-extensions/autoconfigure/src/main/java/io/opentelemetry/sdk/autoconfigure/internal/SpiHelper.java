/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.AutoConfigureListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;
import java.util.function.Function;
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
    return new SpiHelper(ComponentLoader.forClassLoader(classLoader));
  }

  /** Create a {@link SpiHelper} which loads SPIs using the {@code componentLoader}. */
  public static SpiHelper create(ComponentLoader componentLoader) {
    return new SpiHelper(componentLoader);
  }

  /** Return the backing underlying {@link ComponentLoader}. */
  public ComponentLoader getComponentLoader() {
    return componentLoader;
  }

  /**
   * Load implementations of an SPI which are configurable (i.e. they accept {@link
   * ConfigProperties}.
   *
   * @param spiClass the SPI class
   * @param getName function returning the name of an SPI implementation
   * @param getConfigurable function returning a configured instance
   * @param config the configuration to pass to invocations of {@code #getConfigurable}
   * @param <T> the configurable type
   * @param <S> the SPI type
   * @return a {@link NamedSpiManager} used to access configured instances of the SPI by name
   */
  public <T, S> NamedSpiManager<T> loadConfigurable(
      Class<S> spiClass,
      Function<S, String> getName,
      BiFunction<S, ConfigProperties, T> getConfigurable,
      ConfigProperties config) {
    Map<String, Supplier<T>> nameToProvider = new HashMap<>();
    for (S provider : load(spiClass)) {
      String name = getName.apply(provider);
      nameToProvider.put(
          name,
          () -> {
            T result = getConfigurable.apply(provider, config);
            maybeAddListener(result);
            return result;
          });
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
    List<T> result = load(spiClass);
    result.sort(Comparator.comparing(Ordered::order));
    return result;
  }

  /**
   * Load implementations of an SPI.
   *
   * @param spiClass the SPI class
   * @param <T> the SPI type
   * @return list of SPI implementations
   */
  public <T> List<T> load(Class<T> spiClass) {
    List<T> result = new ArrayList<>();
    for (T service : componentLoader.load(spiClass)) {
      maybeAddListener(service);
      result.add(service);
    }
    return result;
  }

  private void maybeAddListener(Object object) {
    if (object instanceof AutoConfigureListener) {
      listeners.add((AutoConfigureListener) object);
    }
  }

  /** Return the set of SPIs loaded which implement {@link AutoConfigureListener}. */
  public Set<AutoConfigureListener> getListeners() {
    return Collections.unmodifiableSet(listeners);
  }
}
