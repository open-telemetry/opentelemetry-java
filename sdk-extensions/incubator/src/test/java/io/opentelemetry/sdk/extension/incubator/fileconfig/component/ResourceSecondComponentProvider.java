/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig.component;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.resources.Resource;

public class ResourceSecondComponentProvider implements ComponentProvider {
  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "order_second";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    return Resource.builder().put("order", "second").build();
  }
}
