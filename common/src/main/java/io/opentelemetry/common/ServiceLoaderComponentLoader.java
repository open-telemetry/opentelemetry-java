/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.common;

import java.util.ServiceLoader;

class ServiceLoaderComponentLoader implements ComponentLoader {

  private final ClassLoader classLoader;

  ServiceLoaderComponentLoader(ClassLoader classLoader) {
    this.classLoader = classLoader;
  }

  @Override
  public <T> Iterable<T> load(Class<T> spiClass) {
    return ServiceLoader.load(spiClass, classLoader);
  }

  @Override
  public String toString() {
    return "ServiceLoaderComponentLoader{classLoader=" + classLoader + "}";
  }
}
