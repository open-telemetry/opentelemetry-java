/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

/**
 * Test ResourceProvider that overrides service.instance.id to test provider ordering. Runs at a
 * higher order than ServiceInstanceIdResourceProvider to ensure it can override the fallback UUID.
 */
public class TestServiceInstanceIdOverrideProvider implements ResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    return Resource.create(
        Attributes.of(AttributeKey.stringKey("service.instance.id"), "override-id"));
  }

  @Override
  public int order() {
    // Run after ServiceInstanceIdResourceProvider (Integer.MIN_VALUE) to override the fallback UUID
    return 0;
  }
}
