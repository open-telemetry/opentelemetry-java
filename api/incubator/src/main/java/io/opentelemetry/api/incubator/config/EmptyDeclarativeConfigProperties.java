/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

import io.opentelemetry.common.ComponentLoader;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/** Empty instance of {@link DeclarativeConfigProperties}. */
final class EmptyDeclarativeConfigProperties implements DeclarativeConfigProperties {

  private static final EmptyDeclarativeConfigProperties INSTANCE =
      new EmptyDeclarativeConfigProperties();
  private static final ComponentLoader COMPONENT_LOADER =
      ComponentLoader.forClassLoader(EmptyDeclarativeConfigProperties.class.getClassLoader());

  private EmptyDeclarativeConfigProperties() {}

  static EmptyDeclarativeConfigProperties getInstance() {
    return INSTANCE;
  }

  @Nullable
  @Override
  public String getString(String name) {
    return null;
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    return null;
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    return null;
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    return null;
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    return null;
  }

  @Nullable
  @Override
  public <T> List<T> getScalarList(String name, Class<T> scalarType) {
    return null;
  }

  @Nullable
  @Override
  public DeclarativeConfigProperties getStructured(String name) {
    return null;
  }

  @Nullable
  @Override
  public List<DeclarativeConfigProperties> getStructuredList(String name) {
    return null;
  }

  @Override
  public Set<String> getPropertyKeys() {
    return Collections.emptySet();
  }

  @Override
  public ComponentLoader getComponentLoader() {
    return COMPONENT_LOADER;
  }
}
