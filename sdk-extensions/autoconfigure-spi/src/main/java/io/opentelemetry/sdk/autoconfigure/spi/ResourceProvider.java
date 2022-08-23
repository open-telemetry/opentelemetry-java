/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi;

import io.opentelemetry.sdk.resources.Resource;

/**
 * A service provider interface (SPI) for providing a {@link Resource} that is merged into the
 * {@linkplain Resource#getDefault() default resource}.
 */
public interface ResourceProvider extends Ordered {

  Resource createResource(ConfigProperties config);

  /**
   * If an implementation needs to apply only under certain conditions related to the config or the
   * existing state of the Resource being built, they can choose to override this default.
   *
   * @param config The auto configuration properties
   * @param existing The current state of the Resource being created
   * @return false to skip over this ResourceProvider, or true to use it
   */
  default boolean shouldApply(ConfigProperties config, Resource existing) {
    return true;
  }
}
