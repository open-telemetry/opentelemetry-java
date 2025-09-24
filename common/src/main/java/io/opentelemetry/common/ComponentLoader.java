/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

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
}
