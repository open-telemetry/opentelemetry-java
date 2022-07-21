/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

/** Interface to be extended by SPIs that need to guarantee ordering during loading. */
public interface Ordered {

  /**
   * Returns the order of applying the SPI implementing this interface. Higher values are added
   * later, for example: an SPI with order=1 will run after an SPI with order=0.
   */
  default int order() {
    return 0;
  }
}
