/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

/**
 * A loader for components that are discovered via SPI.
 *
 * <p>This class is internal and is hence not for public use. Its APIs are unstable and can change
 * at any time.
 */
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
}
