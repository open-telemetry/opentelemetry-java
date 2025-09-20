/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.api.incubator.config;

final class EmptyComponentProviderLoader implements ComponentProviderLoader {

  private static final EmptyComponentProviderLoader INSTANCE = new EmptyComponentProviderLoader();

  EmptyComponentProviderLoader() {}

  static ComponentProviderLoader getInstance() {
    return INSTANCE;
  }

  @Override
  public <T> T loadComponent(Class<T> type, String name, DeclarativeConfigProperties properties) {
    throw new UnsupportedOperationException("Empty ComponentProviderLoader is not implemented");
  }
}
