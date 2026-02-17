/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.ConfigProvider;
import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.common.ComponentLoader;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ExtendedDeclarativeConfigProperties;
import io.opentelemetry.sdk.common.InternalTelemetryVersion;
import io.opentelemetry.sdk.resources.Resource;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/** Declarative configuration context and state carrier. */
class DeclarativeConfigContext {

  private final SpiHelper spiHelper;
  private final List<Closeable> closeables = new ArrayList<>();
  @Nullable private volatile MeterProvider meterProvider;
  @Nullable private Resource resource = null;
  @Nullable private ConfigProvider configProvider;
  @Nullable private List<ComponentProvider> componentProviders = null;

  // Visible for testing
  DeclarativeConfigContext(SpiHelper spiHelper) {
    this.spiHelper = spiHelper;
  }

  static DeclarativeConfigContext create(ComponentLoader componentLoader) {
    return new DeclarativeConfigContext(SpiHelper.create(componentLoader));
  }

  /**
   * Add the {@code closeable} to the list of closeables to clean up if configuration fails
   * exceptionally, and return it.
   */
  <T extends Closeable> T addCloseable(T closeable) {
    closeables.add(closeable);
    return closeable;
  }

  List<Closeable> getCloseables() {
    return Collections.unmodifiableList(closeables);
  }

  public void setMeterProvider(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
  }

  public void setConfigProvider(ConfigProvider configProvider) {
    this.configProvider = configProvider;
  }

  Resource getResource() {
    // called via reflection from io.opentelemetry.sdk.autoconfigure.IncubatingUtil
    if (resource == null) {
      throw new DeclarativeConfigException("Resource has not been configured yet.");
    }
    return resource;
  }

  void setResource(Resource resource) {
    this.resource = resource;
  }

  /**
   * Overload of {@link #setInternalTelemetry(Consumer, Consumer)} for components which do not
   * support setting {@link InternalTelemetryVersion} because they only support {@link
   * InternalTelemetryVersion#LATEST}.
   */
  public void setInternalTelemetry(Consumer<Supplier<MeterProvider>> meterProviderSetter) {
    setInternalTelemetry(meterProviderSetter, unused -> {});
  }

  /**
   * Set internal telemetry on built-in components.
   *
   * @param meterProviderSetter the component meter provider setter
   * @param internalTelemetrySetter the component internal telemetry setter
   */
  public void setInternalTelemetry(
      Consumer<Supplier<MeterProvider>> meterProviderSetter,
      Consumer<InternalTelemetryVersion> internalTelemetrySetter) {
    InternalTelemetryVersion telemetryVersion = getInternalTelemetryVersion();
    if (telemetryVersion != null) {
      meterProviderSetter.accept(() -> Objects.requireNonNull(meterProvider));
      internalTelemetrySetter.accept(telemetryVersion);
    } else {
      meterProviderSetter.accept(MeterProvider::noop);
    }
  }

  @Nullable
  private InternalTelemetryVersion getInternalTelemetryVersion() {
    if (configProvider == null) {
      return null;
    }
    String internalTelemetryVersion =
        configProvider.getInstrumentationConfig("otel_sdk").getString("internal_telemetry_version");
    if (internalTelemetryVersion == null) {
      return null;
    }
    switch (internalTelemetryVersion.toLowerCase(Locale.ROOT)) {
      case "legacy":
        return InternalTelemetryVersion.LEGACY;
      case "latest":
        return InternalTelemetryVersion.LATEST;
      default:
        throw new DeclarativeConfigException(
            "Invalid sdk telemetry version: " + internalTelemetryVersion);
    }
  }

  SpiHelper getSpiHelper() {
    return spiHelper;
  }

  /**
   * Find a registered {@link ComponentProvider} with {@link ComponentProvider#getType()} matching
   * {@code type}, {@link ComponentProvider#getName()} matching {@code name}, and call {@link
   * ComponentProvider#create(DeclarativeConfigProperties)} with the given {@code model}.
   *
   * @throws DeclarativeConfigException if no matching providers are found, or if multiple are found
   *     (i.e. conflict), or if {@link ComponentProvider#create(DeclarativeConfigProperties)} throws
   */
  @SuppressWarnings({"unchecked"})
  <T> T loadComponent(Class<T> type, ConfigKeyValue configKeyValue) {
    String name = configKeyValue.getKey();
    ExtendedDeclarativeConfigProperties config =
        new ExtendedDeclarativeConfigPropertiesImpl(
            configKeyValue.getValue(),
            configProvider == null ? ConfigProvider.noop() : configProvider);

    if (componentProviders == null) {
      componentProviders = spiHelper.load(ComponentProvider.class);
    }
    List<ComponentProvider> matchedProviders =
        componentProviders.stream()
            .filter(
                componentProvider ->
                    componentProvider.getType() == type && name.equals(componentProvider.getName()))
            .collect(Collectors.toList());
    if (matchedProviders.isEmpty()) {
      throw new DeclarativeConfigException(
          "No component provider detected for " + type.getName() + " with name \"" + name + "\".");
    }
    if (matchedProviders.size() > 1) {
      throw new DeclarativeConfigException(
          "Component provider conflict. Multiple providers detected for "
              + type.getName()
              + " with name \""
              + name
              + "\": "
              + componentProviders.stream()
                  .map(provider -> provider.getClass().getName())
                  .collect(Collectors.joining(",", "[", "]")));
    }
    // Exactly one matching component provider
    ComponentProvider provider = matchedProviders.get(0);

    try {
      Object component = provider.create(config);
      if (component instanceof Closeable) {
        closeables.add((Closeable) component);
      }
      if (component != null && !type.isInstance(component)) {
        throw new DeclarativeConfigException(
            "Error configuring "
                + type.getName()
                + " with name \""
                + name
                + "\". Component provider "
                + provider.getClass().getName()
                + " returned an unexpected component type: "
                + component.getClass().getName());
      }
      return (T) component;
    } catch (Throwable throwable) {
      throw new DeclarativeConfigException(
          "Error configuring " + type.getName() + " with name \"" + name + "\"", throwable);
    }
  }

  private static class ExtendedDeclarativeConfigPropertiesImpl
      implements ExtendedDeclarativeConfigProperties {

    private final DeclarativeConfigProperties delegate;
    private final ConfigProvider configProvider;

    ExtendedDeclarativeConfigPropertiesImpl(
        DeclarativeConfigProperties delegate, ConfigProvider configProvider) {
      this.delegate = delegate;
      this.configProvider = configProvider;
    }

    @Override
    public ConfigProvider getConfigProvider() {
      return configProvider;
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

    @Nullable
    @Override
    public <T> List<T> getScalarList(String name, Class<T> scalarType) {
      return delegate.getScalarList(name, scalarType);
    }

    @Nullable
    @Override
    public DeclarativeConfigProperties getStructured(String name) {
      return delegate.getStructured(name);
    }

    @Nullable
    @Override
    public List<DeclarativeConfigProperties> getStructuredList(String name) {
      return delegate.getStructuredList(name);
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
      return "ExtendedDeclarativeConfigPropertiesImpl{" + delegate + '}';
    }
  }
}
