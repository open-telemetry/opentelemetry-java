/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

/**
 * {@link ResourceProvider} for automatically configuring {@link
 * ResourceConfiguration#createEnvironmentResource(ConfigProperties)}.
 *
 * @since 1.47.0
 */
public final class EnvironmentResourceProvider implements ResourceProvider {
  @Override
  public Resource createResource(ConfigProperties config) {
    return ResourceConfiguration.createEnvironmentResource(config);
  }

  @Override
  public int order() {
    // Environment resource takes precedent over all other ResourceProviders except
    // ServiceInstanceIdResourceProvider.
    return Integer.MAX_VALUE - 1;
  }
}
