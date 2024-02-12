/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ConditionalResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Set;

public class SecondResourceProvider implements ConditionalResourceProvider {

  public static final AttributeKey<String> KEY = stringKey("service.name");

  @Override
  public Resource createResource(ConfigProperties config) {
    return Resource.create(Attributes.of(KEY, "test-service-2"));
  }

  @Override
  public int order() {
    return 200;
  }

  @Override
  public Set<AttributeKey<?>> supportedKeys() {
    return Collections.singleton(KEY);
  }

  @Override
  public boolean shouldApply(ConfigProperties config, Resource existing) {
    return !config.getBoolean("skip-second-resource-provider", false);
  }
}
