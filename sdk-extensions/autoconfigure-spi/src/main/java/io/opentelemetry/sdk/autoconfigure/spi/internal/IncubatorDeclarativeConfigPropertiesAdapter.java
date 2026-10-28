/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import io.opentelemetry.api.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

@SuppressWarnings("deprecation")
final class IncubatorDeclarativeConfigPropertiesAdapter
    implements io.opentelemetry.api.incubator.config.DeclarativeConfigProperties {

  private final DeclarativeConfigProperties delegate;

  IncubatorDeclarativeConfigPropertiesAdapter(DeclarativeConfigProperties delegate) {
    this.delegate = delegate;
  }

  @Nullable
  @Override
  public String getString(String name) {
    return delegate.getString(name);
  }

  @Nullable
  @Override
  public Boolean getBoolean(String name) {
    return delegate.getBoolean(name);
  }

  @Nullable
  @Override
  public Integer getInt(String name) {
    return delegate.getInt(name);
  }

  @Nullable
  @Override
  public Long getLong(String name) {
    return delegate.getLong(name);
  }

  @Nullable
  @Override
  public Double getDouble(String name) {
    return delegate.getDouble(name);
  }

  @Override
  public boolean isString(String name) {
    return delegate.isString(name);
  }

  @Override
  public boolean isBoolean(String name) {
    return delegate.isBoolean(name);
  }

  @Override
  public boolean isInt(String name) {
    return delegate.isInt(name);
  }

  @Override
  public boolean isLong(String name) {
    return delegate.isLong(name);
  }

  @Override
  public boolean isDouble(String name) {
    return delegate.isDouble(name);
  }

  @Nullable
  @Override
  public <T> List<T> getScalarList(String name, Class<T> scalarType) {
    return delegate.getScalarList(name, scalarType);
  }

  @Nullable
  @Override
  public DeclarativeConfigProperties getStructured(String name) {
    DeclarativeConfigProperties structured = delegate.getStructured(name);
    return structured == null ? null : new IncubatorDeclarativeConfigPropertiesAdapter(structured);
  }

  @Nullable
  @Override
  public List<DeclarativeConfigProperties> getStructuredList(String name) {
    List<DeclarativeConfigProperties> list = delegate.getStructuredList(name);
    if (list == null) {
      return null;
    }
    List<DeclarativeConfigProperties> adapted = new ArrayList<>(list.size());
    for (DeclarativeConfigProperties item : list) {
      adapted.add(new IncubatorDeclarativeConfigPropertiesAdapter(item));
    }
    return adapted;
  }

  @Override
  public Set<String> getPropertyKeys() {
    return delegate.getPropertyKeys();
  }

  @Override
  public ComponentLoader getComponentLoader() {
    return delegate.getComponentLoader();
  }

  @Override
  public String toString() {
    return "IncubatorDeclarativeConfigPropertiesAdapter{" + delegate + '}';
  }
}
