/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.ComponentProviderLoader;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

class DeclarativeConfigPropertiesWithComponentProviderLoader
    implements DeclarativeConfigProperties {
  private final DeclarativeConfigProperties properties;
  private final ComponentProviderLoader componentProviderLoader;

  DeclarativeConfigPropertiesWithComponentProviderLoader(
      DeclarativeConfigProperties properties, ComponentProviderLoader componentProviderLoader) {
    this.properties = properties;
    this.componentProviderLoader = componentProviderLoader;
  }

  @Nullable
  @Override
  public String getString(String name) {
    return properties.getString(name);
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    return properties.getBoolean(name);
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    return properties.getInt(name);
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    return properties.getLong(name);
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    return properties.getDouble(name);
  }

  @Nullable
  @Override
  public <T> List<T> getScalarList(String name, Class<T> scalarType) {
    return properties.getScalarList(name, scalarType);
  }

  @Nullable
  @Override
  public DeclarativeConfigProperties getStructured(String name) {
    return properties.getStructured(name);
  }

  @Nullable
  @Override
  public List<DeclarativeConfigProperties> getStructuredList(String name) {
    return properties.getStructuredList(name);
  }

  @Override
  public Set<String> getPropertyKeys() {
    return properties.getPropertyKeys();
  }

  @Override
  public ComponentLoader getComponentLoader() {
    return properties.getComponentLoader();
  }

  @Override
  public ComponentProviderLoader getComponentProviderLoader() {
    return componentProviderLoader;
  }
}
