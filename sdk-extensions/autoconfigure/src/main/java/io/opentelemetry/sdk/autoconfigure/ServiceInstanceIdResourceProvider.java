/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.common.internal.SemConvConstants;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collections;
import java.util.UUID;

/**
 * A {@link ResourceProvider} which supplies a {@code service.instance} entity with a random UUID
 * {@code service.instance.id}, stable for the lifetime of the JVM. Runs at {@link
 * Integer#MIN_VALUE} so that any other {@link ResourceProvider} or user configuration which sets
 * {@code service.instance.id} takes precedence.
 */
public final class ServiceInstanceIdResourceProvider implements ResourceProvider {

  private static final Resource RANDOM =
      EntityUtil.createResource(
          Collections.singletonList(
              Entity.builder(
                      SemConvConstants.SERVICE_INSTANCE_TYPE,
                      Attributes.of(
                          SemConvConstants.SERVICE_INSTANCE_ID, UUID.randomUUID().toString()))
                  .setSchemaUrl(SemConvConstants.SCHEMA_URL_V1_40_0)
                  .build()));

  @Override
  public Resource createResource(ConfigProperties config) {
    return RANDOM;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }
}
