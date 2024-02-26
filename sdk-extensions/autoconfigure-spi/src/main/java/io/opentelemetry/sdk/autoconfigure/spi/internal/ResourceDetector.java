/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import java.util.Optional;
import java.util.function.Function;

public interface ResourceDetector<D> extends Ordered {
  /** Read the data for the resource attributes. */
  Optional<D> readData(ConfigProperties config);

  /** Registers the attributes that this resource detector can provide. */
  void registerAttributes(Builder<D> builder);

  /** Greater order means lower priority. The default order is 0. */
  @Override
  default int order() {
    return 0;
  }

  /** Returns the name of this resource detector. */
  String name();

  /**
   * Returns whether this resource detector is enabled by default. If not, it will only be used if
   * explicitly enabled in the configuration.
   */
  default boolean defaultEnabled() {
    return true;
  }

  /** A builder for registering attributes that a resource detector can provide. */
  interface Builder<D> {
    /**
     * Adds an attribute to the resource.
     *
     * @param key the attribute key
     * @param getter a function that returns the value of the attribute from the data that is read
     *     by {@link ResourceDetector#readData(ConfigProperties)}
     * @return this builder
     * @param <T> the type of the attribute
     */
    <T> Builder<D> add(AttributeKey<T> key, Function<D, Optional<T>> getter);
  }
}
