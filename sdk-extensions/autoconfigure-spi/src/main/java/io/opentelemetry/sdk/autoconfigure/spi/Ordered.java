/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.common.ComponentLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Interface to be extended by SPIs that need to guarantee ordering during loading.
 *
 * @since 1.17.0
 */
public interface Ordered {

  /**
   * Returns the order of applying the SPI implementing this interface. Higher values are applied
   * later, for example: an SPI with order=1 will run after an SPI with order=0. SPI implementations
   * with equal values will be run in a non-deterministic order.
   */
  default int order() {
    return 0;
  }

  static <T extends Ordered> List<T> loadOrderedList(
      ComponentLoader componentLoader, Class<T> spiClass) {
    List<T> result = new ArrayList<>(ComponentLoader.loadList(componentLoader, spiClass));
    result.sort(Comparator.comparing(Ordered::order));
    return Collections.unmodifiableList(result);
  }
}
