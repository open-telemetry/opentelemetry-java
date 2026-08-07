/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.EntityExperimentConstants;
import io.opentelemetry.sdk.common.internal.SemConvAttributes;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.resources.ResourceBuilder;
import io.opentelemetry.sdk.resources.internal.Entity;
import io.opentelemetry.sdk.resources.internal.EntityUtil;
import java.util.Collections;
import java.util.UUID;

public class ServiceResourceDetector implements ComponentProvider {

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
    boolean entitiesEnabled =
        properties.getBoolean(EntityExperimentConstants.EXPERIMENTAL_ENTITIES_ENABLED, false);

    String serviceName = properties.getString("otel.service.name");
    if (entitiesEnabled) {
      if (serviceName != null) {
        Entity serviceEntity =
            Entity.builder(
                    SemConvAttributes.SERVICE_TYPE,
                    Attributes.of(SemConvAttributes.SERVICE_NAME, serviceName))
                .setSchemaUrl(SemConvAttributes.SCHEMA_URL_V1_40_0)
                .build();
        EntityUtil.addEntity(builder, serviceEntity);
      }
      Entity serviceInstanceEntity =
          Entity.builder(
                  SemConvAttributes.SERVICE_INSTANCE_TYPE,
                  Attributes.of(SemConvAttributes.SERVICE_INSTANCE_ID, RANDOM_SERVICE_INSTANCE_ID))
              .setSchemaUrl(SemConvAttributes.SCHEMA_URL_V1_40_0)
              .build();
      EntityUtil.addEntity(builder, serviceInstanceEntity);
    } else {
      if (serviceName != null) {
        builder.put(SemConvAttributes.SERVICE_NAME, serviceName);
      }
      builder.put(SemConvAttributes.SERVICE_INSTANCE_ID, RANDOM_SERVICE_INSTANCE_ID);
    }

    return builder.build();
  }
}
