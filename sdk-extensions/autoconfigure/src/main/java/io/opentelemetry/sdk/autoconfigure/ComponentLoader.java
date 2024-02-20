/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigurableProvider;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/** A loader for components that are discovered via SPI. */
public interface ComponentLoader {
  <T> Iterable<T> load(Class<T> spiClass);

  /**
   * Load implementations of an ordered SPI (i.e. implements {@link Ordered}).
   *
   * @param spiClass the SPI class
   * @param <T> the SPI type
   * @return list of SPI implementations, in order
   */
  default <T extends Ordered> List<T> loadOrdered(Class<T> spiClass) {
    return StreamSupport.stream(load(spiClass).spliterator(), false)
        .sorted(Comparator.comparing(Ordered::order))
        .collect(Collectors.toList());
  }

  default <T extends ConfigurableProvider> Map<String, T> loadConfigurableProviders(
      Class<T> spiClass) {
    Map<String, T> components = new HashMap<>();
    for (T component : load(spiClass)) {
      components.put(component.getName(), component);
    }
    return components;
  }
}
