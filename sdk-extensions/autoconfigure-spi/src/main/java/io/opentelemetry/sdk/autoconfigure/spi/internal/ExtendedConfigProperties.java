/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import java.util.List;
import javax.annotation.Nullable;

/**
 * An extended version of {@link ConfigProperties} that supports accessing complex types - nested
 * maps and arrays of maps. {@link ExtendedConfigProperties} is used as a representation of a map,
 * since it has (type safe) accessors for string keys.
 */
public interface ExtendedConfigProperties extends ConfigProperties {

  /**
   * Returns a map-valued configuration property, represented as {@link ExtendedConfigProperties}.
   *
   * @return a map-valued configuration property, or {@code null} if {@code name} has not been
   *     configured.
   * @throws io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException if the property is not a
   *     map
   */
  @Nullable
  default ExtendedConfigProperties getConfigProperties(String name) {
    return null;
  }

  /**
   * Returns a list of map-valued configuration property, represented as {@link
   * ExtendedConfigProperties}.
   *
   * @return a list of map-valued configuration property, or {@code null} if {@code name} has not
   *     been configured.
   * @throws io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException if the property is not a
   *     list of maps
   */
  @Nullable
  default List<ExtendedConfigProperties> getListConfigProperties(String name) {
    return null;
  }
}
