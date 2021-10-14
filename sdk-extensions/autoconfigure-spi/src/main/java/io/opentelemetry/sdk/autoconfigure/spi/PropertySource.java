/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import java.util.Map;

/**
 * A service provider interface (SPI) for providing default values for use in {@link
 * ConfigProperties}. The order of precedence of properties is system properties > environment
 * variables > PropertySource.
 */
public interface PropertySource {
  /**
   * Returns a string map containing the properties to be included in {@link ConfigProperties}. The
   * keys should correspond to recognized system properties.
   */
  Map<String, String> getProperties();
}
