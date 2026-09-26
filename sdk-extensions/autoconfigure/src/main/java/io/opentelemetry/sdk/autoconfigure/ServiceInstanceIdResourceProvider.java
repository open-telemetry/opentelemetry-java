/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.UUID;

/**
 * A {@link ResourceProvider} that supplies {@code service.instance.id} as a fallback when no other
 * provider or user configuration supplies it. This provider generates a random UUID for {@code
 * service.instance.id} only when the attribute has not already been provided by a later
 * ResourceProvider. The value is stable across calls to this provider within the same JVM instance.
 *
 * <p>This provider runs at the lowest priority (Integer.MIN_VALUE) to ensure that any explicitly
 * configured or environment-provided {@code service.instance.id} takes precedence over the fallback
 * UUID.
 */
public final class ServiceInstanceIdResourceProvider implements ResourceProvider {

  public static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  // Multiple calls to this resource provider should return the same value
  private static final Resource RANDOM =
      Resource.create(Attributes.of(SERVICE_INSTANCE_ID, UUID.randomUUID().toString()));

  @Override
  public Resource createResource(ConfigProperties config) {
    return RANDOM;
  }

  @Override
  public int order() {
    // Run first to provide a fallback UUID that can be overridden by later providers
    return Integer.MIN_VALUE;
  }
}
