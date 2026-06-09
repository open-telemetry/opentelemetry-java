/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Auto-configuration for the OpenTelemetry {@link Resource}.
 *
 * @since 1.28.0
 */
public final class ResourceConfiguration {

  private static final boolean ENTITY_INCUBATOR_AVAILABLE;

  static {
    boolean incubatorAvailable = false;
    try {
      Class.forName(
          "io.opentelemetry.sdk.extension.incubator.resources.EntityDetector",
          false,
          ResourceConfiguration.class.getClassLoader());
      incubatorAvailable = true;
    } catch (ClassNotFoundException e) {
      // Not available
    }
    ENTITY_INCUBATOR_AVAILABLE = incubatorAvailable;
  }

  // Visible for testing
  static final String DISABLED_ATTRIBUTE_KEYS = "otel.resource.disabled.keys";
  static final String ENABLED_RESOURCE_PROVIDERS = "otel.java.enabled.resource.providers";
  static final String DISABLED_RESOURCE_PROVIDERS = "otel.java.disabled.resource.providers";

  /**
   * Create a {@link Resource} from the environment. The resource contains attributes parsed from
   * environment variables and system property keys {@code otel.resource.attributes} and {@code
   * otel.service.name}.
   *
   * @return the resource.
   */
  public static Resource createEnvironmentResource() {
    return createEnvironmentResource(
        DefaultConfigProperties.create(
            Collections.emptyMap(),
            ComponentLoader.forClassLoader(ResourceConfiguration.class.getClassLoader())));
  }

  /**
   * Create a {@link Resource} from the environment. The resource contains attributes parsed from
   * environment variables and system property keys {@code otel.resource.attributes} and {@code
   * otel.service.name}.
   *
   * @param config the {@link ConfigProperties} used to obtain resource properties
   * @return the resource.
   */
  public static Resource createEnvironmentResource(ConfigProperties config) {
    return EnvironmentResource.createEnvironmentResource(config);
  }

  static Resource configureResource(
      ConfigProperties config,
      SpiHelper spiHelper,
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer) {
    Resource result = Resource.getDefault();

    Set<String> enabledProviders = new HashSet<>(config.getList(ENABLED_RESOURCE_PROVIDERS));
    Set<String> disabledProviders = new HashSet<>(config.getList(DISABLED_RESOURCE_PROVIDERS));

    // If Entity experiment is enabled, we use a new flow to instantiate resources.
    boolean entitiesEnabled = config.getBoolean("otel.experimental.entities.enabled", false);
    if (entitiesEnabled && ENTITY_INCUBATOR_AVAILABLE) {
      Resource entityResource =
          IncubatingEntityUtil.configureEntityResource(
              config, spiHelper, enabledProviders, disabledProviders);
      if (entityResource != null) {
        result = entityResource;
      }
    } else {

      for (ResourceProvider resourceProvider : spiHelper.loadOrdered(ResourceProvider.class)) {
        if (!enabledProviders.isEmpty()
            && !enabledProviders.contains(resourceProvider.getClass().getName())) {
          continue;
        }
        if (disabledProviders.contains(resourceProvider.getClass().getName())) {
          continue;
        }
        if (resourceProvider instanceof ConditionalResourceProvider
            && !((ConditionalResourceProvider) resourceProvider).shouldApply(config, result)) {
          continue;
        }
        result = result.merge(resourceProvider.createResource(config));
      }

      // TODO(jsuereth): Should filter attributes be used with entities?
      result = filterAttributes(result, config);
    }
    return resourceCustomizer.apply(result, config);
  }

  // visible for testing
  static Resource filterAttributes(Resource resource, ConfigProperties configProperties) {
    List<String> disabledAttibuteKeys = configProperties.getList(DISABLED_ATTRIBUTE_KEYS);
    Set<String> disabledKeys = new HashSet<>(disabledAttibuteKeys);

    ResourceBuilder builder =
        resource.toBuilder().removeIf(attributeKey -> disabledKeys.contains(attributeKey.getKey()));

    if (resource.getSchemaUrl() != null) {
      builder.setSchemaUrl(resource.getSchemaUrl());
    }

    return builder.build();
  }

  private ResourceConfiguration() {}
}
