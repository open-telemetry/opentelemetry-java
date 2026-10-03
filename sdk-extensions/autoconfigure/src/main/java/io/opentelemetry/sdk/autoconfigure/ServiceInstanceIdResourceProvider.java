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
 * A {@link ResourceProvider} which supplies a random UUID {@code service.instance.id}, stable for
 * the lifetime of the JVM. Runs at {@link Integer#MIN_VALUE} so that any other {@link
 * ResourceProvider} or user configuration which sets {@code service.instance.id} takes precedence.
 */
public final class ServiceInstanceIdResourceProvider implements ResourceProvider {

  private static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  private static final Resource RANDOM =
      Resource.create(Attributes.of(SERVICE_INSTANCE_ID, UUID.randomUUID().toString()));

  @Override
  public Resource createResource(ConfigProperties config) {
    return RANDOM;
  }

  @Override
  public int order() {
    return Integer.MIN_VALUE;
  }
}
