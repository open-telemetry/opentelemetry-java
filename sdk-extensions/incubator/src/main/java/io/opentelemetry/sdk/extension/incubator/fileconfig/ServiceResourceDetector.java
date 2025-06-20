/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;

public class ServiceResourceDetector implements ComponentProvider<Resource> {
  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "service";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    ConfigProperties properties = DefaultConfigProperties.create(Collections.emptyMap());
    String serviceName = properties.getString("otel.service.name");
    if (serviceName != null) {
      return Resource.builder().put("service.name", serviceName).build();
    }
    return Resource.empty();
  }
}
