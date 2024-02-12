/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.Set;

public class FirstResourceProvider implements ResourceProvider {

  @SuppressWarnings("NonFinalStaticField")
  public static int calls = 0;

  public static final AttributeKey<String> KEY = stringKey("service.name");

  @Override
  public Resource createResource(ConfigProperties config) {
    calls++;
    return Resource.create(Attributes.of(KEY, "test-service"));
  }

  @Override
  public int order() {
    return 100;
  }

  @Override
  public Set<AttributeKey<?>> supportedKeys() {
    return Collections.singleton(KEY);
  }
}
