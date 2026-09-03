/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.resources;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.UUID;

/**
 * A {@link ConditionalResourceProvider} for {@code service.instance.id}. It implements {@link
 * ConditionalResourceProvider} rather than a plain {@link ResourceProvider} because it depends on
 * the attributes discovered by the other providers.
 *
 * <p>This provider generates a random UUID for {@code service.instance.id} if not already set by
 * the user or another resource provider. The value is stable across calls to this provider within
 * the same JVM instance.
 *
 * @since 1.47.0
 */
public final class ServiceInstanceIdResourceProvider implements ConditionalResourceProvider {

  public static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  // multiple calls to this resource provider should return the same value
  private static final Resource RANDOM =
      Resource.create(Attributes.of(SERVICE_INSTANCE_ID, UUID.randomUUID().toString()));

  static final int ORDER = Integer.MAX_VALUE;

  @Override
  public Resource createResource(ConfigProperties config) {
    return RANDOM;
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    return existing.getAttribute(SERVICE_INSTANCE_ID) == null;
  }

  @Override
  public int order() {
    // Run after environment resource provider - only set the service instance ID if it
    // hasn't been set by any other provider or the user.
    return ORDER;
  }
}
