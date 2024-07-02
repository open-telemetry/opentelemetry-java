/*
 * Copyright The OpenTelemetry Authors
 * SPDX-License-Identifier: Apache-2.0
 */

package io.opentelemetry.sdk.extension.incubator.fileconfig;

import io.opentelemetry.api.incubator.config.StructuredConfigException;
import io.opentelemetry.api.incubator.config.StructuredConfigProperties;
import io.opentelemetry.sdk.autoconfigure.internal.SpiHelper;
import io.opentelemetry.sdk.autoconfigure.spi.internal.ComponentProvider;
import java.io.Closeable;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.Nullable;

final class FileConfigUtil {

  private FileConfigUtil() {}

  /** Add the {@code closeable} to the {@code closeables} and return it. */
  static <T> T addAndReturn(List<Closeable> closeables, T closeable) {
    if (closeable instanceof Closeable) {
      closeables.add((Closeable) closeable);
    }
    return closeable;
  }

  static <T> T assertNotNull(@Nullable T object, String description) {
    if (object == null) {
      throw new NullPointerException(description + " is null");
    }
    return object;
  }

  /**
   * Find a registered {@link ComponentProvider} which {@link ComponentProvider#getType()} matching
   * {@code type}, {@link ComponentProvider#getName()} matching {@code name}, and call {@link
   * ComponentProvider#create(StructuredConfigProperties)} with the given {@code model}.
   *
   * @throws StructuredConfigException if no matching providers are found, or if multiple are found
   *     (i.e. conflict), or if {@link ComponentProvider#create(StructuredConfigProperties)} throws
   */
  @SuppressWarnings({"unchecked", "rawtypes"})
  static <T> T loadComponent(SpiHelper spiHelper, Class<T> type, String name, Object model) {
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
      throw new StructuredConfigException(
          "No component provider detected for " + type.getName() + " with name \"" + name + "\".");
    }
    if (matchedProviders.size() > 1) {
      throw new StructuredConfigException(
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

    // Map model to generic structured config properties
    StructuredConfigProperties config = FileConfiguration.toConfigProperties(model);

    try {
      return provider.create(config);
    } catch (Throwable throwable) {
      throw new StructuredConfigException(
          "Error configuring " + type.getName() + " with name \"" + name + "\"", throwable);
    }
  }
}
