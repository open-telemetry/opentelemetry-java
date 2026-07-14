/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.resources;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.UUID;

/**
 * does not implement {@link ResourceProvider}, because it depends on all attributes discovered by
 * the other providers.
 */
public final class ServiceInstanceIdResourceProvider implements ConditionalResourceProvider {

  // multiple calls to this resource provider should return the same value
  private static final String RANDOM_SERVICE_INSTANCE_ID = UUID.randomUUID().toString();

  static final int ORDER = Integer.MAX_VALUE;

  @Override
  public Resource createResource(ConfigProperties config) {
    return EntityUtil.addEntity(
            Resource.builder(),
            Entity.builder(
                    SemConvAttributes.SERVICE_INSTANCE_TYPE,
                    Attributes.of(
                        SemConvAttributes.SERVICE_INSTANCE_ID, RANDOM_SERVICE_INSTANCE_ID))
                .setSchemaUrl(SemConvAttributes.SCHEMA_URL_V1_40_0)
                .build())
        .build();
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    return existing.getAttribute(SemConvAttributes.SERVICE_INSTANCE_ID) == null;
  }

  @Override
  public int order() {
    // Run after environment resource provider - only set the service instance ID if it
    // hasn't been set by any other provider or the user.
    return ORDER;
  }
}
