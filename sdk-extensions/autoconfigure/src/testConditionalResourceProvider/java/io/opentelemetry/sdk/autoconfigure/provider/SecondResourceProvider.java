/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

public class SecondResourceProvider implements ConditionalResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    return Resource.create(Attributes.of(stringKey("service.name"), "test-service-2"));
  }

  @Override
  public int order() {
    return 200;
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    String serviceName = existing.getAttribute(stringKey("service.name"));
    return serviceName == null || "unknown_service:java".equals(serviceName);
  }
}
