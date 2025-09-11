/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.common.AttributesBuilder;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigurationException;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiFunction;

/**
 * Auto-configuration for the OpenTelemetry {@link Resource}.
 *
 * @since 1.28.0
 */
public final class ResourceConfiguration {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");

  // Visible for testing
  static final String ATTRIBUTE_PROPERTY = "otel.resource.attributes";
  static final String SERVICE_NAME_PROPERTY = "otel.service.name";
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
    AttributesBuilder resourceAttributes = Attributes.builder();
    try {
      for (Map.Entry<String, String> entry : config.getMap(ATTRIBUTE_PROPERTY).entrySet()) {
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
    String serviceName = config.getString(SERVICE_NAME_PROPERTY);
    if (serviceName != null) {
      resourceAttributes.put(SERVICE_NAME, serviceName);
    }

    return Resource.create(resourceAttributes.build());
  }

  static Resource configureResource(
      ConfigProperties config,
      SpiHelper spiHelper,
      BiFunction<? super Resource, ConfigProperties, ? extends Resource> resourceCustomizer) {
    Resource result = Resource.getDefault();

    Set<String> enabledProviders = new HashSet<>(config.getList(ENABLED_RESOURCE_PROVIDERS));
    Set<String> disabledProviders = new HashSet<>(config.getList(DISABLED_RESOURCE_PROVIDERS));

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

    result = filterAttributes(result, config);

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
