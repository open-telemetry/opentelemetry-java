/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.provider;

import static io.opentelemetry.api.common.AttributeKey.stringKey;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ResourceProvider;
import io.opentelemetry.sdk.resources.Resource;

public class SecondResourceProvider implements ResourceProvider {

  @Override
  public Resource createResource(ConfigProperties config) {
    return Resource.create(Attributes.of(stringKey("otel.some_resource"), "still unused"));
  }

  @Override
  public int order() {
    return 200;
  }
}
