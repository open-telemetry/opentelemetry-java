/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.declarativeconfig;

import static io.opentelemetry.sdk.autoconfigure.declarativeconfig.EnvironmentResource.ENTITIES_PROPERTY;
import static io.opentelemetry.sdk.autoconfigure.spi.internal.EntityExperimentConstants.EXPERIMENTAL_ENTITIES_ENABLED;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.ConfigProperties;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.DefaultConfigProperties;
import io.opentelemetry.sdk.resources.Resource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class EntitiesEnvResourceDetector implements ComponentProvider {

  @Override
  public Class<Resource> getType() {
    return Resource.class;
  }

  @Override
  public String getName() {
    return "env";
  }

  @Override
  public Resource create(DeclarativeConfigProperties config) {
    ConfigProperties envConfigProperties =
        DefaultConfigProperties.create(Collections.emptyMap(), config.getComponentLoader());

    Map<String, String> configPropertiesMap = new HashMap<>();
    configPropertiesMap.put(ENTITIES_PROPERTY, envConfigProperties.getString(ENTITIES_PROPERTY));
    configPropertiesMap.put(EXPERIMENTAL_ENTITIES_ENABLED, "true");

    return EnvironmentResource.createEnvironmentResource(
        DefaultConfigProperties.createFromMap(configPropertiesMap));
  }
}
