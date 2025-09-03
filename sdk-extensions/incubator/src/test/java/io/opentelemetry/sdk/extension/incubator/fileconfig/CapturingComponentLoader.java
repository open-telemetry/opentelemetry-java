/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A test utility that captures the configuration passed to component providers during loading. This
 * allows tests to verify that component providers receive the expected configuration.
 */
class CapturingComponentLoader implements ComponentLoader {

  private final ComponentLoader delegate;
  private final Map<String, DeclarativeConfigProperties> capturedConfigs =
      new ConcurrentHashMap<>();

  CapturingComponentLoader() {
    delegate = ComponentLoader.forClassLoader(getClass().getClassLoader());
  }

  DeclarativeConfigProperties getCapturedConfig(String name) {
    return capturedConfigs.get(name);
  }

  @Override
  public <T> Iterable<T> load(Class<T> spiClass) {
    if (spiClass == ComponentProvider.class) {
      return createWrappedComponentProviders();
    }
    return delegate.load(spiClass);
  }

  @SuppressWarnings("unchecked")
  private <T> Iterable<T> createWrappedComponentProviders() {
    List<T> wrappedProviders = new ArrayList<>();
    for (ComponentProvider provider : delegate.load(ComponentProvider.class)) {
      ComponentProvider wrapped = new CapturingComponentProvider(provider);
      wrappedProviders.add((T) wrapped);
    }
    return wrappedProviders;
  }

  private class CapturingComponentProvider implements ComponentProvider {

    private final ComponentProvider delegate;

    CapturingComponentProvider(ComponentProvider delegate) {
      this.delegate = delegate;
    }

    @Override
    public Class<?> getType() {
      return delegate.getType();
    }

    @Override
    public String getName() {
      return delegate.getName();
    }

    @Override
    public Object create(DeclarativeConfigProperties config) {
      capturedConfigs.put(getName(), config);
      return delegate.create(config);
    }
  }
}
