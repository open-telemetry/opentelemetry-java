/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.autoconfigure.spi.internal;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.annotation.Nullable;

/** Empty instance of {@link StructuredConfigProperties}. */
final class EmptyStructuredConfigProperties implements StructuredConfigProperties {

  private static final EmptyStructuredConfigProperties INSTANCE =
      new EmptyStructuredConfigProperties();

  private EmptyStructuredConfigProperties() {}

  static EmptyStructuredConfigProperties getInstance() {
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
  public StructuredConfigProperties getStructured(String name) {
    return null;
  }

  @Nullable
  @Override
  public List<StructuredConfigProperties> getStructuredList(String name) {
    return null;
  }

  @Override
  public Set<String> getPropertyKeys() {
    return Collections.emptySet();
  }
}
