/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashSet;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Auto-configuration for the OpenTelemetry {@link Resource}.
 *
 * @deprecated Use {@link AutoConfiguredOpenTelemetrySdk#getResource()}.
 */
@Deprecated
public final class OpenTelemetryResourceAutoConfiguration {

  /**
   * Returns the automatically configured {@link Resource}.
   *
   * <p>This method will auto-configure the returned {@link Resource} using system properties and
   * environment variables.
   *
   * @deprecated Use {@link AutoConfiguredOpenTelemetrySdk#getResource()}.
   */
  @Deprecated
  public static Resource configureResource() {
    return configureResource(DefaultConfigProperties.get(Collections.emptyMap()));
  }

  /**
   * Returns a {@link Resource} automatically initialized through recognized system properties and
   * environment variables.
   *
   * @deprecated Use {@link AutoConfiguredOpenTelemetrySdk#getResource()}.
   */
  @Deprecated
  public static Resource configureResource(ConfigProperties config) {
    return configureResource(config, (a, unused) -> a);
  }

  @SuppressWarnings("deprecation") // Uses class which will be made package private
  static Resource configureResource(
      ConfigProperties config,
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer) {
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

    return resourceCustomizer.apply(result, config);
  }

  private OpenTelemetryResourceAutoConfiguration() {}
}
