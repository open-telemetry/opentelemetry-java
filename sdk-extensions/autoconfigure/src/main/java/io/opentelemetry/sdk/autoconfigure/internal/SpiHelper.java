/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

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

/**
 * This class is internal and is hence not for public use. Its APIs are unstable and can change at
 * any time.
 */
public final class SpiHelper {

  private final ClassLoader classLoader;
  private final SpiFinder spiFinder;

  // Visible for testing
  SpiHelper(ClassLoader classLoader, SpiFinder spiFinder) {
    this.classLoader = classLoader;
    this.spiFinder = spiFinder;
  }

  /** Create a {@link SpiHelper} which loads SPIs using the {@code classLoader}. */
  public static SpiHelper create(ClassLoader classLoader) {
    return new SpiHelper(classLoader, ServiceLoader::load);
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
      nameToProvider.put(name, () -> getConfigurable.apply(provider, config));
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
    for (T service : spiFinder.load(spiClass, classLoader)) {
      result.add(service);
    }
    return result;
  }

  // Visible for testing
  interface SpiFinder {
    <T> Iterable<T> load(Class<T> spiClass, ClassLoader classLoader);
  }
}
