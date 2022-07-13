/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiFunction;

/** Auto-configuration for the OpenTelemetry {@link Resource}. */
final class ResourceConfiguration {

  // Visible for testing
  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";
  static final String DISABLED_ATTRIBUTE_KEYS = "otel.experimental.resource.disabled.keys";

  static Resource configureResource(
      ConfigProperties config,
      ClassLoader serviceClassLoader,
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer) {
    Resource result = Resource.getDefault();

    Set<String> enabledProviders =
        new HashSet<>(config.getList("otel.java.enabled.resource.providers"));
    // TODO(anuraaga): We use a hyphen only once in this artifact, for
    // otel.java.disabled.resource-providers. But fetching by the dot version is the simplest way
    // to implement it for now.
    Set<String> disabledProviders =
        new HashSet<>(config.getList("otel.java.disabled.resource.providers"));
    for (ResourceProvider resourceProvider :
        SpiUtil.loadOrdered(ResourceProvider.class, serviceClassLoader)) {
      if (!enabledProviders.isEmpty()
          && !enabledProviders.contains(resourceProvider.getClass().getName())) {
        continue;
      }
      if (disabledProviders.contains(resourceProvider.getClass().getName())) {
        continue;
      }
      result = result.merge(resourceProvider.createResource(config));
    }

    result = result.merge(createEnvironmentResource(config));

    result = filterAttributes(result, config);

    return resourceCustomizer.apply(result, config);
  }

  private static Resource createEnvironmentResource(ConfigProperties config) {
    return Resource.create(getAttributes(config), ResourceAttributes.SCHEMA_URL);
  }

  // visible for testing
  static Attributes getAttributes(ConfigProperties configProperties) {
    AttributesBuilder resourceAttributes = Attributes.builder();
    configProperties.getMap(ATTRIBUTE_PROPERTY).forEach(resourceAttributes::put);
    String serviceName = configProperties.getString(SERVICE_NAME_PROPERTY);
    if (serviceName != null) {
      resourceAttributes.put(ResourceAttributes.SERVICE_NAME, serviceName);
    }
    return resourceAttributes.build();
  }

  // visible for testing
  static Resource filterAttributes(Resource resource, ConfigProperties configProperties) {
    Set<String> disabledKeys = new HashSet<>(configProperties.getList(DISABLED_ATTRIBUTE_KEYS));

    ResourceBuilder builder =
        resource.toBuilder().removeIf(attributeKey -> disabledKeys.contains(attributeKey.getKey()));

    if (resource.getSchemaUrl() != null) {
      builder.setSchemaUrl(resource.getSchemaUrl());
    }

    return builder.build();
  }

  private ResourceConfiguration() {}
}
