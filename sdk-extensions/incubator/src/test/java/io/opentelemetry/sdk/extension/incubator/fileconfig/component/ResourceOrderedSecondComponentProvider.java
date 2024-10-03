/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.Ordered;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.resources.Resource;

public class ResourceOrderedSecondComponentProvider
    implements ComponentProvider<Resource>, Ordered {
  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "unused";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    return Resource.builder().put("order", "second").build();
  }

  @Override
  public int order() {
    return 2;
  }
}
