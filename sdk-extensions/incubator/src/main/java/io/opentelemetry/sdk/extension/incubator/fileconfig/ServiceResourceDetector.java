/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.common.AttributeKey;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import java.util.Collections;
import java.util.UUID;

public class ServiceResourceDetector implements ComponentProvider {

  private static final AttributeKey<String> SERVICE_NAME = AttributeKey.stringKey("service.name");
  private static final AttributeKey<String> SERVICE_INSTANCE_ID =
      AttributeKey.stringKey("service.instance.id");

  // multiple calls to this resource provider should return the same value
  private static final String RANDOM_SERVICE_INSTANCE_ID = UUID.randomUUID().toString();

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
    ResourceBuilder builder = Resource.builder();

    ConfigProperties properties =
        DefaultConfigProperties.create(Collections.emptyMap(), config.getComponentLoader());
    String serviceName = properties.getString("otel.service.name");
    if (serviceName != null) {
      builder.put(SERVICE_NAME, serviceName).build();
    }

    builder.put(SERVICE_INSTANCE_ID, RANDOM_SERVICE_INSTANCE_ID);

    return builder.build();
  }
}
