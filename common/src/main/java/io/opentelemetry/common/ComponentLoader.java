/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ServiceLoader;

/** A loader for components that are discovered via SPI. */
public interface ComponentLoader {
  /**
   * Load implementations of an SPI.
   *
   * @param spiClass the SPI class
   * @param <T> the SPI type
   * @return iterable of SPI implementations
   */
  <T> Iterable<T> load(Class<T> spiClass);

  /**
   * Create an instance for the {@code classLoader} using {@link ServiceLoader#load(Class,
   * ClassLoader)}.
   */
  static ComponentLoader forClassLoader(ClassLoader classLoader) {
    return new ServiceLoaderComponentLoader(classLoader);
  }

  static <T> List<T> loadList(ComponentLoader componentLoader, Class<T> spiClass) {
    List<T> result = new ArrayList<>();
    componentLoader.load(spiClass).forEach(result::add);
    return Collections.unmodifiableList(result);
  }
}
