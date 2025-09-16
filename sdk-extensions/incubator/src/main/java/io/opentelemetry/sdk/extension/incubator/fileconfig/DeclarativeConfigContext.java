/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.DeclarativeConfigException;
import io.opentelemetry.api.incubator.config.DeclarativeConfigProperties;
import io.opentelemetry.api.metrics.MeterProvider;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

/** Declarative configuration context and state carrier. */
class DeclarativeConfigContext {

  private final SpiHelper spiHelper;
  private final List<Closeable> closeables = new ArrayList<>();
  @Nullable private volatile MeterProvider meterProvider;

  DeclarativeConfigContext(SpiHelper spiHelper) {
    this.spiHelper = spiHelper;
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

  @Nullable
  public MeterProvider getMeterProvider() {
    return meterProvider;
  }

  public void setMeterProvider(MeterProvider meterProvider) {
    this.meterProvider = meterProvider;
  }

  /**
   * Find a registered {@link ComponentProvider} with {@link ComponentProvider#getType()} matching
   * {@code type}, {@link ComponentProvider#getName()} matching {@code name}, and call {@link
   * ComponentProvider#create(DeclarativeConfigProperties)} with the given {@code model}.
   *
   * @throws DeclarativeConfigException if no matching providers are found, or if multiple are found
   *     (i.e. conflict), or if {@link ComponentProvider#create(DeclarativeConfigProperties)} throws
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  <T> T loadComponent(Class<T> type, String name, Object model) {
    DeclarativeConfigProperties config =
        DeclarativeConfiguration.toConfigProperties(model, spiHelper.getComponentLoader());

    // TODO(jack-berg): cache loaded component providers
    List<ComponentProvider> componentProviders = spiHelper.load(ComponentProvider.class);
    List<ComponentProvider<?>> matchedProviders =
        componentProviders.stream()
            .map(
                (Function<ComponentProvider, ComponentProvider<?>>)
                    componentProvider -> componentProvider)
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
    ComponentProvider<T> provider = (ComponentProvider<T>) matchedProviders.get(0);

    try {
      return provider.create(config);
    } catch (Throwable throwable) {
      throw new DeclarativeConfigException(
          "Error configuring " + type.getName() + " with name \"" + name + "\"", throwable);
    }
  }
}
