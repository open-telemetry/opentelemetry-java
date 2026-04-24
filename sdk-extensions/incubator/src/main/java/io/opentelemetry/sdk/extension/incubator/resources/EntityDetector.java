/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collection;

/**
 * A service provider interface (SPI) for providing a collection of {@link Entity} that are merged
 * into the {@link Resource#getDefault() default resource}.
 */
public interface EntityDetector extends Ordered {
  /**
   * Detects entities based on the configuration.
   *
   * @param config the configuration to use for detection
   */
  Collection<Entity> detect(ConfigProperties config);
}
