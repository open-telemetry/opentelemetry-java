/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

public class FirstResourceProvider implements ConditionalResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    return Resource.create(Attributes.of(ResourceAttributes.SERVICE_NAME, "test-service"));
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    return !config.getBoolean("skip-first-resource-provider", false);
  }
}
