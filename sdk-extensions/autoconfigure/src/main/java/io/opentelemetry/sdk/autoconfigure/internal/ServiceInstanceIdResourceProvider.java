/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.internal;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.UUID;

/**
 * does not implement {@link ResourceProvider}, because it depends on all attributes discovered by
 * the other providers.
 */
public final class ServiceInstanceIdResourceProvider implements ConditionalResourceProvider {

  public static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  // multiple calls to this resource provider should return the same value
  public static final Resource RANDOM =
      Resource.create(Attributes.of(SERVICE_INSTANCE_ID, UUID.randomUUID().toString()));

  @Override
  public Resource createResource(ConfigProperties config) {
    return RANDOM;
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    return config.getString(SERVICE_INSTANCE_ID.getKey()) == null
        && existing.getAttribute(SERVICE_INSTANCE_ID) == null;
  }

  @Override
  public int order() {
    // Run just before the environment resource provider - only set the service instance ID if it
    // hasn't been set by any other provider.
    return Integer.MAX_VALUE - 1;
  }
}
