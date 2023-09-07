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

public class FirstResourceProvider implements ConditionalResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    return Resource.create(Attributes.of(stringKey("service.name"), "test-service"));
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
