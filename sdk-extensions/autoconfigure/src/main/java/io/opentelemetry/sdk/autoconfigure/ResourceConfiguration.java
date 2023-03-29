/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Map;
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
      if (resourceProvider instanceof ConditionalResourceProvider
          && !((ConditionalResourceProvider) resourceProvider).shouldApply(config, result)) {
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
    try {
      for (Map.Entry<String, String> entry :
          configProperties.getMap(ATTRIBUTE_PROPERTY).entrySet()) {
        resourceAttributes.put(
            entry.getKey(),
            // Attributes specified via otel.resource.attributes follow the W3C Baggage spec and
            // characters outside the baggage-octet range are percent encoded
            // https://github.com/open-telemetry/opentelemetry-specification/blob/main/specification/resource/sdk.md#specifying-resource-information-via-an-environment-variable
            URLDecoder.decode(entry.getValue(), StandardCharsets.UTF_8.displayName()));
      }
    } catch (UnsupportedEncodingException e) {
      // Should not happen since always using standard charset
      throw new ConfigurationException("Unable to decode resource attributes.", e);
    }
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
