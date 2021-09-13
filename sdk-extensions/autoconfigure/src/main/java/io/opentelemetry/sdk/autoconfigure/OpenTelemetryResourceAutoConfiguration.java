/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;

/** Auto-configuration for the OpenTelemetry {@link Resource}. */
public final class OpenTelemetryResourceAutoConfiguration {

  /**
   * Returns the automatically configured {@link Resource}.
   *
   * <p>This method will auto-configure the returned {@link Resource} using system properties and
   * environment variables.
   */
  public static Resource configureResource() {
    return configureResource(DefaultConfigProperties.get());
  }

  /**
   * Returns a {@link Resource} automatically initialized through recognized system properties and
   * environment variables.
   */
  public static Resource configureResource(ConfigProperties config) {
    Resource result = Resource.getDefault();

    // TODO(anuraaga): We use a hyphen only once in this artifact, for
    // otel.java.disabled.resource-providers. But fetching by the dot version is the simplest way
    // to implement it for now.
    Set<String> disabledProviders =
        new HashSet<>(config.getList("otel.java.disabled.resource.providers"));
    for (ResourceProvider resourceProvider : ServiceLoader.load(ResourceProvider.class)) {
      if (disabledProviders.contains(resourceProvider.getClass().getName())) {
        continue;
      }
      result = result.merge(resourceProvider.createResource(config));
    }

    result = result.merge(EnvironmentResource.create(config));

    return result;
  }

  private OpenTelemetryResourceAutoConfiguration() {}
}
